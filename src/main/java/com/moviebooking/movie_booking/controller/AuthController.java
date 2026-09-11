package com.moviebooking.movie_booking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        return userService.registerUser(user);
    }
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
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