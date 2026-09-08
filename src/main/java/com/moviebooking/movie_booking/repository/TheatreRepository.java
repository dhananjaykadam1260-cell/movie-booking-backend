package com.moviebooking.movie_booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.moviebooking.movie_booking.model.Theatre;

public interface TheatreRepository extends JpaRepository<Theatre, Long> {
}