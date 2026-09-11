package com.moviebooking.movie_booking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.moviebooking.movie_booking.model.Screen;
import com.moviebooking.movie_booking.model.Theatre;
import com.moviebooking.movie_booking.repository.ScreenRepository;
import com.moviebooking.movie_booking.repository.ShowRepository;
import com.moviebooking.movie_booking.repository.TheatreRepository;

@Service
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final TheatreRepository theatreRepository;
    private final ShowRepository showRepository;

    public ScreenService(
            ScreenRepository screenRepository,
            TheatreRepository theatreRepository,
            ShowRepository showRepository) {

        this.screenRepository = screenRepository;
        this.theatreRepository = theatreRepository;
        this.showRepository = showRepository;
    }

    public Screen addScreen(Long theatreId, Screen screen) {

        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() ->
                        new RuntimeException("Theatre not found"));

        screen.setTheatre(theatre);

        return screenRepository.save(screen);
    }

    public List<Screen> getAllScreens() {
        return screenRepository.findAll();
    }

    public Screen updateScreen(
            Long id,
            Long theatreId,
            Screen updatedScreen) {

        Screen screen = screenRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Screen not found"));

        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() ->
                        new RuntimeException("Theatre not found"));

        screen.setScreenName(updatedScreen.getScreenName());
        screen.setTotalSeats(updatedScreen.getTotalSeats());
        screen.setTheatre(theatre);

        return screenRepository.save(screen);
    }

    public void deleteScreen(Long id) {

        Screen screen = screenRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Screen not found"));

        if (showRepository.existsByScreenId(id)) {
            throw new RuntimeException(
                    "Cannot delete screen because shows are assigned to it"
            );
        }

        screenRepository.delete(screen);
    }
}