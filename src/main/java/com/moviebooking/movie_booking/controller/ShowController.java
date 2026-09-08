package com.moviebooking.movie_booking.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.moviebooking.movie_booking.model.Show;
import com.moviebooking.movie_booking.service.ShowService;

@RestController
@RequestMapping("/api/shows")
@CrossOrigin(origins = "http://localhost:5173")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping
    public Show addShow(
            @RequestParam Long movieId,
            @RequestParam Long theatreId,
            @RequestBody Show show) {

        return showService.addShow(movieId, theatreId, show);
    }

    @GetMapping
    public List<Show> getAllShows() {
        return showService.getAllShows();
    }
    
    @GetMapping("/movie/{movieId}")
    public List<Show> getShowsByMovie(@PathVariable Long movieId) {
        return showService.getShowsByMovie(movieId);
    }
}