package com.moviebooking.movie_booking.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.moviebooking.movie_booking.dto.AuthResponse;
import com.moviebooking.movie_booking.dto.LoginRequest;
import com.moviebooking.movie_booking.dto.RegisterRequest;
import com.moviebooking.movie_booking.dto.UserResponse;
import com.moviebooking.movie_booking.exception.ApiException;
import com.moviebooking.movie_booking.model.User;
import com.moviebooking.movie_booking.repository.UserRepository;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    public void registerUser(RegisterRequest request) {

        String name = request.name() == null ? "" : request.name().trim();
        String email = request.email() == null ? "" : request.email().trim();
        String password = request.password() == null ? "" : request.password();

        if (name.isEmpty() || email.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Name and email are required.");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "An account with this email already exists.");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        // Always USER. Admins are promoted directly in the database.
        user.setRole("USER");

        userRepository.save(user);

        // The account exists even if the welcome email fails.
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        } catch (Exception e) {
            log.warn("Welcome email failed for {}: {}", user.getEmail(), e.getMessage());
        }
    }

    public AuthResponse loginUser(LoginRequest request) {

        User user = request.email() == null
                ? null
                : userRepository.findByEmail(request.email().trim());

        // Same message for unknown email and wrong password.
        if (user == null || request.password() == null
                || !passwordEncoder.matches(request.password(), user.getPassword())) {

            throw new ApiException(HttpStatus.UNAUTHORIZED,
                    "Invalid email or password.");
        }

        return new AuthResponse(
                jwtService.generateToken(user.getEmail()),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole());
    }

    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getName(),
                        u.getEmail(),
                        u.getRole()))
                .toList();
    }
}
