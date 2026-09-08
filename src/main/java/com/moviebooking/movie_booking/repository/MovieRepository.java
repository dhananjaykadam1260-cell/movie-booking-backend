package com.moviebooking.movie_booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.moviebooking.movie_booking.model.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}