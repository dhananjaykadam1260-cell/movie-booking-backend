package com.moviebooking.movie_booking.dto;

/** Only these fields can be set at sign-up. Role is always USER. */
public record RegisterRequest(String name, String email, String password) {}
