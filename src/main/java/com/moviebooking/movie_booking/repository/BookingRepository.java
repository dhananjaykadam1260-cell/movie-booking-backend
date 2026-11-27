package com.moviebooking.movie_booking.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.moviebooking.movie_booking.model.Booking;

public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    boolean existsByShowId(Long showId);

    List<Booking> findByStatus(String status);

    /** Confirmed bookings whose show date is on or before the given date. */
    @Query("select b from Booking b join b.show s " +
           "where b.status = 'CONFIRMED' and s.showDate <= :date")
    List<Booking> findConfirmedShowsOnOrBefore(@Param("date") LocalDate date);
}
