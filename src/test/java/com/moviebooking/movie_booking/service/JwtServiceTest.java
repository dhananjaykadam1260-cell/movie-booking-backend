package com.moviebooking.movie_booking.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET =
            "test-secret-that-is-definitely-longer-than-32-bytes";

    @Test
    void tokenRoundTripReturnsEmail() {
        JwtService jwt = new JwtService(SECRET, 60_000);

        String token = jwt.generateToken("ann@example.com");

        assertEquals("ann@example.com", jwt.parseEmail(token).orElseThrow());
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService jwt = new JwtService(SECRET, 60_000);

        String token = jwt.generateToken("ann@example.com") + "x";

        assertTrue(jwt.parseEmail(token).isEmpty());
    }

    @Test
    void tokenSignedWithOtherSecretIsRejected() {
        String token = new JwtService(SECRET, 60_000)
                .generateToken("ann@example.com");

        JwtService other = new JwtService(
                "another-secret-that-is-also-longer-than-32-bytes", 60_000);

        assertTrue(other.parseEmail(token).isEmpty());
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwt = new JwtService(SECRET, -1_000);

        assertTrue(jwt.parseEmail(jwt.generateToken("ann@example.com")).isEmpty());
    }

    @Test
    void shortSecretFailsFast() {
        assertThrows(IllegalStateException.class,
                () -> new JwtService("too-short", 60_000));
    }
}
