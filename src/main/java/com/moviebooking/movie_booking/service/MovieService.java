package com.moviebooking.movie_booking.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.moviebooking.movie_booking.exception.ApiException;
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
                        new ApiException(HttpStatus.NOT_FOUND, "Movie not found."));
    }

    public Movie updateMovie(Long id, Movie updatedMovie) {

        Movie movie = getMovieById(id);

        movie.setTitle(updatedMovie.getTitle());
        movie.setLanguage(updatedMovie.getLanguage());
        movie.setGenre(updatedMovie.getGenre());
        movie.setDuration(updatedMovie.getDuration());
        movie.setImageUrl(updatedMovie.getImageUrl());
        movie.setDescription(updatedMovie.getDescription());

        return movieRepository.save(movie);
    }

    public void deleteMovie(Long id) {

        Movie movie = getMovieById(id);

        if (showRepository.existsByMovieId(id)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot delete movie because shows are assigned to it.");
        }

        movieRepository.delete(movie);
    }
}
