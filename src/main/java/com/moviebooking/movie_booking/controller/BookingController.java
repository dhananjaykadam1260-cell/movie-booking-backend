package com.moviebooking.movie_booking.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.moviebooking.movie_booking.dto.BookingRequest;
import com.moviebooking.movie_booking.model.Booking;
import com.moviebooking.movie_booking.security.AuthUser;
import com.moviebooking.movie_booking.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(
            BookingService bookingService) {

        this.bookingService = bookingService;
    }

    @PostMapping
    public Booking bookSeats(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody BookingRequest request) {

        return bookingService.bookSeats(user, request);
    }

    @GetMapping("/user/{userId}")
    public List<Booking> getUserBookings(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long userId) {

        return bookingService.getUserBookings(user, userId);
    }

    /** Admin only (enforced in SecurityConfig). */
    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/{id}/ticket")
    public ResponseEntity<byte[]> downloadTicket(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long id) {

        byte[] pdf = bookingService.downloadTicket(user, id);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Movie-Ticket-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/{id}/cancel")
    public Booking cancelBooking(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long id) {

        return bookingService.cancelBooking(user, id);
    }
}
