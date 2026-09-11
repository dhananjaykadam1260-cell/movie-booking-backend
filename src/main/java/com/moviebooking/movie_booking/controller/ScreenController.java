package com.moviebooking.movie_booking.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.moviebooking.movie_booking.model.Screen;
import com.moviebooking.movie_booking.service.ScreenService;

@RestController
@RequestMapping("/api/screens")
@CrossOrigin(origins = "http://localhost:5173")
public class ScreenController {

    private final ScreenService screenService;

    public ScreenController(ScreenService screenService) {
        this.screenService = screenService;
    }

    @PostMapping
    public Screen addScreen(
            @RequestParam Long theatreId,
            @RequestBody Screen screen) {

        return screenService.addScreen(theatreId, screen);
    }

    @GetMapping
    public List<Screen> getAllScreens() {
        return screenService.getAllScreens();
    }
    
    @PutMapping("/{id}")
    public Screen updateScreen(
            @PathVariable Long id,
            @RequestParam Long theatreId,
            @RequestBody Screen screen) {

        return screenService.updateScreen(
                id,
                theatreId,
                screen
        );
    }

    @DeleteMapping("/{id}")
    public String deleteScreen(@PathVariable Long id) {

        screenService.deleteScreen(id);

        return "Screen deleted successfully";
    }
    
}