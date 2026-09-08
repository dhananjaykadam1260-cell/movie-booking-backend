package com.moviebooking.movie_booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.moviebooking.movie_booking.model.Show;

public interface ShowRepository extends JpaRepository<Show, Long> {
}