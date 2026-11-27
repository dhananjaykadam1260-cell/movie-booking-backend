package com.moviebooking.movie_booking.security;

/**
 * The authenticated caller, built from the database on every request
 * (not from the token), so role changes take effect immediately.
 */
public record AuthUser(Long id, String email, String role) {

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
