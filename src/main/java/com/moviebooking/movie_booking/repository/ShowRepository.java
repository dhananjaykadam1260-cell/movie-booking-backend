package com.moviebooking.movie_booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moviebooking.movie_booking.model.Show;

public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByMovieId(Long movieId);

    boolean existsByMovieId(Long movieId);

    boolean existsByScreenId(Long screenId);

    boolean existsByTheatreId(Long theatreId);
}