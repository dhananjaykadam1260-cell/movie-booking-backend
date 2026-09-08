package com.moviebooking.movie_booking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.moviebooking.movie_booking.model.Movie;
import com.moviebooking.movie_booking.model.Show;
import com.moviebooking.movie_booking.model.Theatre;
import com.moviebooking.movie_booking.repository.MovieRepository;
import com.moviebooking.movie_booking.repository.ShowRepository;
import com.moviebooking.movie_booking.repository.TheatreRepository;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;

    public ShowService(
            ShowRepository showRepository,
            MovieRepository movieRepository,
            TheatreRepository theatreRepository) {

        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.theatreRepository = theatreRepository;
    }

    public Show addShow(Long movieId, Long theatreId, Show show) {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new RuntimeException("Theatre not found"));

        show.setMovie(movie);
        show.setTheatre(theatre);

        return showRepository.save(show);
    }

    public List<Show> getAllShows() {
        return showRepository.findAll();
    }
    
    public List<Show> getShowsByMovie(Long movieId) {
        return showRepository.findByMovieId(movieId);
    }
}