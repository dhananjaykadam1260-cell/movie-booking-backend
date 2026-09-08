package com.moviebooking.movie_booking.service;

import java.util.List;

import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new RuntimeException("Show not found"));

        seat.setShow(show);

        return seatRepository.save(seat);
    }

    public List<Seat> getSeatsByShow(Long showId) {
        return seatRepository.findByShowId(showId);
    }
}