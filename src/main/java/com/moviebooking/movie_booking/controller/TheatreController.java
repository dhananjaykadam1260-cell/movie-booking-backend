package com.moviebooking.movie_booking.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.moviebooking.movie_booking.model.Theatre;
import com.moviebooking.movie_booking.service.TheatreService;

@RestController
@RequestMapping("/api/theatres")
@CrossOrigin(origins = "http://localhost:5173")
public class TheatreController {

    private final TheatreService theatreService;

    public TheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    @PostMapping
    public Theatre addTheatre(@RequestBody Theatre theatre) {
        return theatreService.addTheatre(theatre);
    }

    @GetMapping
    public List<Theatre> getAllTheatres() {
        return theatreService.getAllTheatres();
    }

    @PutMapping("/{id}")
    public Theatre updateTheatre(
            @PathVariable Long id,
            @RequestBody Theatre theatre) {

        return theatreService.updateTheatre(id, theatre);
    }

    @DeleteMapping("/{id}")
    public String deleteTheatre(@PathVariable Long id) {

        theatreService.deleteTheatre(id);

        return "Theatre deleted successfully";
    }
}