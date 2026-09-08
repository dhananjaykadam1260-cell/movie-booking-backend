package com.moviebooking.movie_booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moviebooking.movie_booking.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    User findByEmail(String email);
}