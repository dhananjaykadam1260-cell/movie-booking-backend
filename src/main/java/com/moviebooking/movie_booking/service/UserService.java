package com.moviebooking.movie_booking.service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.moviebooking.movie_booking.dto.RegisterRequest;
import com.moviebooking.movie_booking.model.User;
import com.moviebooking.movie_booking.repository.UserRepository;

@Service
public class UserService {

    private static final Logger log =
            LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final EmailVerificationService emailVerificationService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailService emailService,
            EmailVerificationService emailVerificationService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.emailVerificationService = emailVerificationService;
    }

    public void sendRegistrationOtp(String email) {
        emailVerificationService.sendOtp(email);
    }

    public String verifyRegistrationOtp(
            String email,
            String otp) {

        return emailVerificationService.verifyOtp(email, otp);
    }

    public String registerUser(RegisterRequest request) {

        if (request.getName() == null ||
                request.getName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Name is required."
            );
        }

        if (request.getPassword() == null ||
                request.getPassword().length() < 6) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must contain at least 6 characters."
            );
        }

        if (request.getEmail() == null ||
                request.getEmail().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is required."
            );
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists."
            );
        }

        boolean verified =
                emailVerificationService.consumeVerificationToken(
                        email,
                        request.getVerificationToken()
                );

        if (!verified) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please verify your email before registering."
            );
        }

        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Never accept an ADMIN role from the registration form.
        user.setRole("USER");

        userRepository.save(user);

        try {
            emailService.sendWelcomeEmail(
                    user.getEmail(),
                    user.getName()
            );
        } catch (Exception e) {
            // Registration succeeded; a welcome-email failure
            // should not make the client think account creation failed.
            log.warn(
                    "Welcome email could not be sent to {}",
                    user.getEmail(),
                    e
            );
        }

        return "Registration successful. You can now log in.";
    }

    public Map<String, Object> loginUser(
            String email,
            String password) {

        if (email == null || password == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email and password are required."
            );
        }

        User user = userRepository.findByEmail(
                email.trim().toLowerCase(Locale.ROOT)
        );

        if (user == null ||
                !passwordEncoder.matches(
                        password,
                        user.getPassword())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password."
            );
        }

        String token = jwtService.generateToken(user.getEmail());

        Map<String, Object> response = new HashMap<>();

        response.put("token", token);
        response.put("userId", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());

        return response;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}