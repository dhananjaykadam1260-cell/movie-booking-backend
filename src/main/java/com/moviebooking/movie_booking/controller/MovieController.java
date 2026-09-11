package com.moviebooking.movie_booking.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.moviebooking.movie_booking.model.Movie;
import com.moviebooking.movie_booking.service.MovieService;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "http://localhost:5173")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public Movie addMovie(@RequestBody Movie movie) {
        return movieService.addMovie(movie);
    }

    @GetMapping
    public List<Movie> getAllMovies() {
        return movieService.getAllMovies();
    }
    @GetMapping("/{id}")
    public Movie getMovieById(@PathVariable Long id) {
        return movieService.getMovieById(id);
    }
    
    @PutMapping("/{id}")
    public Movie updateMovie(
            @PathVariable Long id,
            @RequestBody Movie movie) {

        return movieService.updateMovie(id, movie);
    }

    @DeleteMapping("/{id}")
    public String deleteMovie(@PathVariable Long id) {

        movieService.deleteMovie(id);

        return "Movie deleted successfully";
    }
    
}