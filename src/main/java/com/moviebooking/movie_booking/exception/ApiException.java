package com.moviebooking.movie_booking.exception;

import org.springframework.http.HttpStatus;

/**
 * Business-rule failure with an explicit HTTP status.
 * Thrown by services and turned into a JSON error by GlobalExceptionHandler.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
