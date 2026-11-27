package com.moviebooking.movie_booking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.moviebooking.movie_booking.dto.AuthResponse;
import com.moviebooking.movie_booking.dto.LoginRequest;
import com.moviebooking.movie_booking.dto.RegisterRequest;
import com.moviebooking.movie_booking.dto.UserResponse;
import com.moviebooking.movie_booking.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@RequestBody RegisterRequest request) {

        userService.registerUser(request);

        return Map.of("message", "Registration successful");
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return userService.loginUser(request);
    }

    /** Admin only (enforced in SecurityConfig). */
    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }
}
