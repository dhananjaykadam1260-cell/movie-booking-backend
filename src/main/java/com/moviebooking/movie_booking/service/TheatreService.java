package com.moviebooking.movie_booking.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.moviebooking.movie_booking.exception.ApiException;
import com.moviebooking.movie_booking.model.Theatre;
import com.moviebooking.movie_booking.repository.ScreenRepository;
import com.moviebooking.movie_booking.repository.ShowRepository;
import com.moviebooking.movie_booking.repository.TheatreRepository;

@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;
    private final ShowRepository showRepository;
    private final ScreenRepository screenRepository;

    public TheatreService(
            TheatreRepository theatreRepository,
            ShowRepository showRepository,
            ScreenRepository screenRepository) {

        this.theatreRepository = theatreRepository;
        this.showRepository = showRepository;
        this.screenRepository = screenRepository;
    }

    public Theatre addTheatre(Theatre theatre) {
        return theatreRepository.save(theatre);
    }

    public List<Theatre> getAllTheatres() {
        return theatreRepository.findAll();
    }

    private Theatre getTheatre(Long id) {
        return theatreRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "Theatre not found."));
    }

    public Theatre updateTheatre(Long id, Theatre updatedTheatre) {

        Theatre theatre = getTheatre(id);

        theatre.setName(updatedTheatre.getName());
        theatre.setCity(updatedTheatre.getCity());
        theatre.setAddress(updatedTheatre.getAddress());

        return theatreRepository.save(theatre);
    }

    public void deleteTheatre(Long id) {

        Theatre theatre = getTheatre(id);

        if (showRepository.existsByTheatreId(id)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot delete theatre because shows are assigned to it.");
        }

        if (screenRepository.existsByTheatreId(id)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Delete theatre screens first.");
        }

        theatreRepository.delete(theatre);
    }
}
