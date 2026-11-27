package com.moviebooking.movie_booking.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.moviebooking.movie_booking.model.User;
import com.moviebooking.movie_booking.repository.UserRepository;
import com.moviebooking.movie_booking.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Reads "Authorization: Bearer <jwt>", validates it, and loads the user.
 * Invalid or missing tokens leave the request anonymous; protected
 * endpoints then return 401.
 *
 * Not a @Component on purpose: it is added to the security chain in
 * SecurityConfig only, so Spring Boot does not also register it as a
 * servlet filter.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER)) {

            String token = header.substring(BEARER.length());

            jwtService.parseEmail(token).ifPresent(email -> {

                User user = userRepository.findByEmail(email);

                if (user != null) {

                    AuthUser principal = new AuthUser(
                            user.getId(),
                            user.getEmail(),
                            user.getRole());

                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    List.of(new SimpleGrantedAuthority(
                                            "ROLE_" + user.getRole()))));
                }
            });
        }

        chain.doFilter(request, response);
    }
}
