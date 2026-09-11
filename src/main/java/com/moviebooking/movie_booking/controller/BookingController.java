package com.moviebooking.movie_booking.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.moviebooking.movie_booking.dto.BookingRequest;
import com.moviebooking.movie_booking.model.Booking;
import com.moviebooking.movie_booking.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:5173")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(
            BookingService bookingService) {

        this.bookingService = bookingService;
    }

    @PostMapping
    public Booking bookSeats(
            @RequestBody BookingRequest request) {

        return bookingService.bookSeats(request);
    }

    @GetMapping("/user/{userId}")
    public List<Booking> getUserBookings(
            @PathVariable Long userId) {

        return bookingService
                .getUserBookings(userId);
    }

    @GetMapping
    public List<Booking> getAllBookings() {

        return bookingService
                .getAllBookings();
    }

    @GetMapping("/{id}/ticket")
    public ResponseEntity<byte[]> downloadTicket(
            @PathVariable Long id) {

        byte[] pdf =
                bookingService.downloadTicket(id);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Movie-Ticket-"
                                + id
                                + ".pdf"
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(pdf);
    }
}