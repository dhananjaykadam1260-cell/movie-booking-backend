package com.moviebooking.movie_booking.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.moviebooking.movie_booking.model.User;
import com.moviebooking.movie_booking.repository.UserRepository;
import java.util.List;

@Service
public class UserService {

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

    public String registerUser(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            return "Email already exists";
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        // Normal registrations should be USER
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }

        userRepository.save(user);

        emailService.sendWelcomeEmail(
                user.getEmail(),
                user.getName()
        );

        return "Registration successful";
    }

    public Map<String, Object> loginUser(
            String email,
            String password) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!passwordEncoder.matches(
                password,
                user.getPassword())) {

            throw new RuntimeException("Invalid password");
        }

        String token =
                jwtService.generateToken(email);

        Map<String, Object> response =
                new HashMap<>();

        response.put("token", token);
        response.put("userId", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());

        // Important for admin login
        response.put("role", user.getRole());

        return response;
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}