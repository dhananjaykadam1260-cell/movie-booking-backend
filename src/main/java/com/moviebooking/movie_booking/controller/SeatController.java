package com.moviebooking.movie_booking.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.moviebooking.movie_booking.model.Seat;
import com.moviebooking.movie_booking.service.SeatService;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping
    public Seat addSeat(
            @RequestParam Long showId,
            @RequestBody Seat seat) {

        return seatService.addSeat(showId, seat);
    }

    @GetMapping("/show/{showId}")
    public List<Seat> getSeats(@PathVariable Long showId) {
        return seatService.getSeatsByShow(showId);
    }
}