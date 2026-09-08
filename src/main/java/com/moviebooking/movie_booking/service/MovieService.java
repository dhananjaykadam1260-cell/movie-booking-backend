package com.moviebooking.movie_booking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.moviebooking.movie_booking.model.Movie;
import com.moviebooking.movie_booking.repository.MovieRepository;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public Movie addMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }
}