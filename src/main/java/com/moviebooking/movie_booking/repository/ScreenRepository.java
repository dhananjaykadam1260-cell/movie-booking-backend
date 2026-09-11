package com.moviebooking.movie_booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moviebooking.movie_booking.model.Screen;

public interface ScreenRepository extends JpaRepository<Screen, Long> {

    boolean existsByTheatreId(Long theatreId);
}