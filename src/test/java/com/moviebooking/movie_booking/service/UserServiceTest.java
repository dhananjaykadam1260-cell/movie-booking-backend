package com.moviebooking.movie_booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.moviebooking.movie_booking.dto.LoginRequest;
import com.moviebooking.movie_booking.dto.RegisterRequest;
import com.moviebooking.movie_booking.exception.ApiException;
import com.moviebooking.movie_booking.model.User;
import com.moviebooking.movie_booking.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock JwtService jwtService;
    @Mock EmailService emailService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, encoder, jwtService, emailService);
    }

    @Test
    void registeredUserAlwaysGetsUserRole() {
        when(userRepository.existsByEmail("ann@example.com")).thenReturn(false);

        userService.registerUser(
                new RegisterRequest("Ann", "ann@example.com", "password123"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());

        assertEquals("USER", saved.getValue().getRole());
        assertNotEquals("password123", saved.getValue().getPassword());
        assertTrue(encoder.matches("password123", saved.getValue().getPassword()));
    }

    @Test
    void duplicateEmailIsConflict() {
        when(userRepository.existsByEmail("ann@example.com")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () ->
                userService.registerUser(
                        new RegisterRequest("Ann", "ann@example.com", "password123")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shortPasswordIsRejected() {
        ApiException ex = assertThrows(ApiException.class, () ->
                userService.registerUser(
                        new RegisterRequest("Ann", "ann@example.com", "short")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void welcomeEmailFailureDoesNotFailRegistration() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        doThrow(new RuntimeException("smtp down"))
                .when(emailService).sendWelcomeEmail(any(), any());

        assertDoesNotThrow(() -> userService.registerUser(
                new RegisterRequest("Ann", "ann@example.com", "password123")));

        verify(userRepository).save(any(User.class));
    }

    @Test
    void wrongPasswordAndUnknownEmailGiveSameMessage() {
        User user = new User();
        user.setEmail("ann@example.com");
        user.setPassword(encoder.encode("password123"));
        when(userRepository.findByEmail("ann@example.com")).thenReturn(user);
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(null);

        ApiException wrongPassword = assertThrows(ApiException.class, () ->
                userService.loginUser(new LoginRequest("ann@example.com", "wrong")));
        ApiException unknown = assertThrows(ApiException.class, () ->
                userService.loginUser(new LoginRequest("nobody@example.com", "x")));

        assertEquals(HttpStatus.UNAUTHORIZED, wrongPassword.getStatus());
        assertEquals(wrongPassword.getMessage(), unknown.getMessage());
    }

    @Test
    void successfulLoginReturnsToken() {
        User user = new User();
        user.setId(7L);
        user.setName("Ann");
        user.setEmail("ann@example.com");
        user.setPassword(encoder.encode("password123"));
        user.setRole("USER");
        when(userRepository.findByEmail("ann@example.com")).thenReturn(user);
        when(jwtService.generateToken("ann@example.com")).thenReturn("tok");

        var response = userService.loginUser(
                new LoginRequest("ann@example.com", "password123"));

        assertEquals("tok", response.token());
        assertEquals(7L, response.userId());
        assertEquals("USER", response.role());
    }
}
