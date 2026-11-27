package com.moviebooking.movie_booking.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.moviebooking.movie_booking.exception.ApiException;
import com.moviebooking.movie_booking.model.Seat;
import com.moviebooking.movie_booking.model.Show;
import com.moviebooking.movie_booking.repository.SeatRepository;
import com.moviebooking.movie_booking.repository.ShowRepository;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;

    public SeatService(
            SeatRepository seatRepository,
            ShowRepository showRepository) {

        this.seatRepository = seatRepository;
        this.showRepository = showRepository;
    }

    public Seat addSeat(Long showId, Seat seat) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "Show not found."));

        // New seats are always free and belong to the requested show.
        seat.setId(null);
        seat.setBooked(false);
        seat.setShow(show);

        return seatRepository.save(seat);
    }

    public List<Seat> getSeatsByShow(Long showId) {
        return seatRepository.findByShowId(showId);
    }
}
