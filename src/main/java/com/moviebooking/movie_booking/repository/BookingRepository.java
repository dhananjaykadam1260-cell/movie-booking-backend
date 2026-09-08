package com.moviebooking.movie_booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.moviebooking.movie_booking.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);
}