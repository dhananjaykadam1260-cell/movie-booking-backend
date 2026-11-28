package com.moviebooking.movie_booking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.moviebooking.movie_booking.dto.RegisterRequest;
import com.moviebooking.movie_booking.dto.SendOtpRequest;
import com.moviebooking.movie_booking.dto.VerifyOtpRequest;
import com.moviebooking.movie_booking.model.User;
import com.moviebooking.movie_booking.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/send-otp")
    public Map<String, String> sendOtp(
            @RequestBody SendOtpRequest request) {

        userService.sendRegistrationOtp(request.getEmail());

        return Map.of(
                "message",
                "OTP sent successfully. Please check your email."
        );
    }

    @PostMapping("/verify-otp")
    public Map<String, String> verifyOtp(
            @RequestBody VerifyOtpRequest request) {

        String verificationToken =
                userService.verifyRegistrationOtp(
                        request.getEmail(),
                        request.getOtp()
                );

        return Map.of(
                "message", "Email verified successfully.",
                "verificationToken", verificationToken
        );
    }

    @PostMapping("/register")
    public String register(
            @RequestBody RegisterRequest request) {

        return userService.registerUser(request);
    }

    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestBody User user) {

        return userService.loginUser(
                user.getEmail(),
                user.getPassword()
        );
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}