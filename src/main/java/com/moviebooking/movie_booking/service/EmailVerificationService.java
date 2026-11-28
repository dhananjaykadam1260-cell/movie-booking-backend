package com.moviebooking.movie_booking.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.moviebooking.movie_booking.repository.UserRepository;

@Service
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    private final SecureRandom random = new SecureRandom();

    private final ConcurrentHashMap<String, OtpEntry> otpStore =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, TokenEntry> tokenStore =
            new ConcurrentHashMap<>();

    public EmailVerificationService(
            UserRepository userRepository,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public void sendOtp(String inputEmail) {

        String email = normalizeEmail(inputEmail);

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please enter a valid email address."
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This email is already registered."
            );
        }

        cleanupExpiredEntries();

        String otp = String.format(
                Locale.ROOT,
                "%06d",
                random.nextInt(1_000_000)
        );

        OtpEntry entry = new OtpEntry(
                otp,
                Instant.now().plusSeconds(300)
        );

        otpStore.put(email, entry);

        try {
            emailService.sendVerificationOtp(email, otp);
        } catch (Exception e) {
            otpStore.remove(email, entry);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to send OTP. Please check your email configuration."
            );
        }
    }

    public synchronized String verifyOtp(
            String inputEmail,
            String inputOtp) {

        String email = normalizeEmail(inputEmail);

        if (inputOtp == null || inputOtp.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please enter the OTP."
            );
        }

        OtpEntry entry = otpStore.get(email);

        if (entry == null ||
                !entry.expiresAt.isAfter(Instant.now())) {

            otpStore.remove(email);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "OTP expired or not found. Please request a new OTP."
            );
        }

        if (entry.attempts >= 5) {
            otpStore.remove(email);

            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many incorrect attempts. Request a new OTP."
            );
        }

        if (!entry.otp.equals(inputOtp.trim())) {

            entry.attempts++;

            if (entry.attempts >= 5) {
                otpStore.remove(email);

                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Too many incorrect attempts. Request a new OTP."
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Incorrect OTP. Please try again."
            );
        }

        otpStore.remove(email, entry);

        String verificationToken = UUID.randomUUID().toString();

        tokenStore.put(
                verificationToken,
                new TokenEntry(
                        email,
                        Instant.now().plusSeconds(600)
                )
        );

        return verificationToken;
    }

    public boolean consumeVerificationToken(
            String inputEmail,
            String verificationToken) {

        if (verificationToken == null ||
                verificationToken.isBlank()) {
            return false;
        }

        String email = normalizeEmail(inputEmail);

        TokenEntry entry = tokenStore.get(verificationToken);

        if (entry == null) {
            return false;
        }

        if (!entry.expiresAt.isAfter(Instant.now())) {
            tokenStore.remove(verificationToken, entry);
            return false;
        }

        if (!entry.email.equals(email)) {
            return false;
        }

        // Token can only be used once.
        return tokenStore.remove(verificationToken, entry);
    }

    private String normalizeEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email address is required."
            );
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void cleanupExpiredEntries() {

        Instant now = Instant.now();

        otpStore.entrySet().removeIf(
                entry -> !entry.getValue().expiresAt.isAfter(now)
        );

        tokenStore.entrySet().removeIf(
                entry -> !entry.getValue().expiresAt.isAfter(now)
        );
    }

    private static class OtpEntry {

        private final String otp;
        private final Instant expiresAt;
        private int attempts = 0;

        private OtpEntry(String otp, Instant expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }
    }

    private static class TokenEntry {

        private final String email;
        private final Instant expiresAt;

        private TokenEntry(String email, Instant expiresAt) {
            this.email = email;
            this.expiresAt = expiresAt;
        }
    }
}