package com.moviebooking.movie_booking.service;

import java.util.List;

import org.springframework.stereotype.Service;

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

    public Theatre updateTheatre(
            Long id,
            Theatre updatedTheatre) {

        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Theatre not found"));

        theatre.setName(updatedTheatre.getName());
        theatre.setCity(updatedTheatre.getCity());
        theatre.setAddress(updatedTheatre.getAddress());

        return theatreRepository.save(theatre);
    }

    public void deleteTheatre(Long id) {

        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Theatre not found"));

        if (showRepository.existsByTheatreId(id)) {
            throw new RuntimeException(
                    "Cannot delete theatre because shows are assigned to it"
            );
        }

        if (screenRepository.existsByTheatreId(id)) {
            throw new RuntimeException(
                    "Delete theatre screens first"
            );
        }

        theatreRepository.delete(theatre);
    }
}