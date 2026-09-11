package com.moviebooking.movie_booking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.moviebooking.movie_booking.model.Movie;
import com.moviebooking.movie_booking.repository.MovieRepository;
import com.moviebooking.movie_booking.repository.ShowRepository;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final ShowRepository showRepository;

    public MovieService(
            MovieRepository movieRepository,
            ShowRepository showRepository) {

        this.movieRepository = movieRepository;
        this.showRepository = showRepository;
    }

    public Movie addMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Movie not found"));
    }

    public Movie updateMovie(Long id, Movie updatedMovie) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Movie not found"));

        movie.setTitle(updatedMovie.getTitle());
        movie.setLanguage(updatedMovie.getLanguage());
        movie.setGenre(updatedMovie.getGenre());
        movie.setDuration(updatedMovie.getDuration());
        movie.setImageUrl(updatedMovie.getImageUrl());
        movie.setDescription(updatedMovie.getDescription());

        return movieRepository.save(movie);
    }

    public void deleteMovie(Long id) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Movie not found"));

        if (showRepository.existsByMovieId(id)) {
            throw new RuntimeException(
                    "Cannot delete movie because shows are assigned to it"
            );
        }

        movieRepository.delete(movie);
    }
}