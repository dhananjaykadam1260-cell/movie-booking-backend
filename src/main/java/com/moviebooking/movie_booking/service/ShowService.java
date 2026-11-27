package com.moviebooking.movie_booking.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moviebooking.movie_booking.exception.ApiException;
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

    private static final int SEATS_PER_ROW = 10;

    // Rows are lettered A-Z, so a screen can have at most 26 rows.
    private static final int MAX_SEATS = 26 * SEATS_PER_ROW;

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
                        new ApiException(HttpStatus.NOT_FOUND, "Movie not found."));

        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "Theatre not found."));

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "Screen not found."));

        // Make sure screen belongs to selected theatre
        if (screen.getTheatre() == null ||
                !screen.getTheatre().getId().equals(theatreId)) {

            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Selected screen does not belong to this theatre.");
        }

        if (screen.getTotalSeats() < 1 || screen.getTotalSeats() > MAX_SEATS) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Screen must have between 1 and " + MAX_SEATS + " seats.");
        }

        show.setId(null);
        show.setMovie(movie);
        show.setTheatre(theatre);
        show.setScreen(screen);

        Show savedShow = showRepository.save(show);

        generateSeats(savedShow, screen.getTotalSeats());

        return savedShow;
    }

    private void generateSeats(
            Show show,
            int totalSeats) {

        List<Seat> seats = new ArrayList<>();

        for (int i = 0; i < totalSeats; i++) {

            char row = (char) ('A' + (i / SEATS_PER_ROW));
            int seatNumber = (i % SEATS_PER_ROW) + 1;

            Seat seat = new Seat();

            seat.setSeatNumber(row + String.valueOf(seatNumber));
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
                        new ApiException(HttpStatus.NOT_FOUND, "Show not found."));

        if (bookingRepository.existsByShowId(showId)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot delete show because bookings already exist.");
        }

        List<Seat> seats = seatRepository.findByShowId(showId);

        seatRepository.deleteAll(seats);

        showRepository.delete(show);
    }
}
