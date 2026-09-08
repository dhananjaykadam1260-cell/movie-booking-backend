package com.moviebooking.movie_booking.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.moviebooking.movie_booking.model.Theatre;
import com.moviebooking.movie_booking.repository.TheatreRepository;

@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;

    public TheatreService(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }

    public Theatre addTheatre(Theatre theatre) {
        return theatreRepository.save(theatre);
    }

    public List<Theatre> getAllTheatres() {
        return theatreRepository.findAll();
    }
}