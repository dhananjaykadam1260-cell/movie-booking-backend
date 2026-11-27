package com.moviebooking.movie_booking.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.moviebooking.movie_booking.exception.ApiException;
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

    private Theatre getTheatre(Long theatreId) {
        return theatreRepository.findById(theatreId)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "Theatre not found."));
    }

    private Screen getScreen(Long id) {
        return screenRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "Screen not found."));
    }

    public Screen addScreen(Long theatreId, Screen screen) {

        screen.setTheatre(getTheatre(theatreId));

        return screenRepository.save(screen);
    }

    public List<Screen> getAllScreens() {
        return screenRepository.findAll();
    }

    public Screen updateScreen(Long id, Long theatreId, Screen updatedScreen) {

        Screen screen = getScreen(id);
        Theatre theatre = getTheatre(theatreId);

        screen.setScreenName(updatedScreen.getScreenName());
        screen.setTotalSeats(updatedScreen.getTotalSeats());
        screen.setTheatre(theatre);

        return screenRepository.save(screen);
    }

    public void deleteScreen(Long id) {

        Screen screen = getScreen(id);

        if (showRepository.existsByScreenId(id)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot delete screen because shows are assigned to it.");
        }

        screenRepository.delete(screen);
    }
}
