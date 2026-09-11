package com.moviebooking.movie_booking.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moviebooking.movie_booking.model.Movie;
import com.moviebooking.movie_booking.model.Screen;
import com.moviebooking.movie_booking.model.Seat;
import com.moviebooking.movie_booking.model.Show;
import com.moviebooking.movie_booking.model.Theatre;
import com.moviebooking.movie_booking.repository.BookingRepository;
import com.moviebooking.movie_booking.repository.MovieRepository;
import com.moviebooking.movie_booking.repository.ScreenRepository;
import com.moviebooking.movie_booking.repository.SeatRepository;
import com.moviebooking.movie_booking.repository.ShowRepository;
import com.moviebooking.movie_booking.repository.TheatreRepository;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;

    public ShowService(
            ShowRepository showRepository,
            MovieRepository movieRepository,
            TheatreRepository theatreRepository,
            ScreenRepository screenRepository,
            SeatRepository seatRepository,
            BookingRepository bookingRepository) {

        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.theatreRepository = theatreRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public Show addShow(
            Long movieId,
            Long theatreId,
            Long screenId,
            Show show) {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() ->
                        new RuntimeException("Movie not found"));

        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() ->
                        new RuntimeException("Theatre not found"));

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() ->
                        new RuntimeException("Screen not found"));

        // Make sure screen belongs to selected theatre
        if (screen.getTheatre() == null ||
                !screen.getTheatre().getId().equals(theatreId)) {

            throw new RuntimeException(
                    "Selected screen does not belong to this theatre"
            );
        }

        show.setMovie(movie);
        show.setTheatre(theatre);
        show.setScreen(screen);

        Show savedShow = showRepository.save(show);

        generateSeats(
                savedShow,
                screen.getTotalSeats()
        );

        return savedShow;
    }

    private void generateSeats(
            Show show,
            int totalSeats) {

        List<Seat> seats = new ArrayList<>();

        int seatsPerRow = 10;

        for (int i = 0; i < totalSeats; i++) {

            char row =
                    (char) ('A' + (i / seatsPerRow));

            int seatNumber =
                    (i % seatsPerRow) + 1;

            Seat seat = new Seat();

            seat.setSeatNumber(
                    row + String.valueOf(seatNumber)
            );

            seat.setBooked(false);
            seat.setShow(show);

            seats.add(seat);
        }

        seatRepository.saveAll(seats);
    }

    public List<Show> getAllShows() {
        return showRepository.findAll();
    }

    public List<Show> getShowsByMovie(Long movieId) {
        return showRepository.findByMovieId(movieId);
    }

    @Transactional
    public void deleteShow(Long showId) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() ->
                        new RuntimeException("Show not found"));

        if (bookingRepository.existsByShowId(showId)) {

            throw new RuntimeException(
                    "Cannot delete show because bookings already exist"
            );
        }

        List<Seat> seats =
                seatRepository.findByShowId(showId);

        seatRepository.deleteAll(seats);

        showRepository.delete(show);
    }
}