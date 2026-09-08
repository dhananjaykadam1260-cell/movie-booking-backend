package com.moviebooking.movie_booking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moviebooking.movie_booking.dto.BookingRequest;
import com.moviebooking.movie_booking.model.*;
import com.moviebooking.movie_booking.repository.*;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            ShowRepository showRepository,
            SeatRepository seatRepository) {

        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public Booking bookSeats(BookingRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found"));

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());

        if (seats.size() != request.getSeatIds().size()) {
            throw new RuntimeException("Invalid seat selected");
        }

        for (Seat seat : seats) {

            if (!seat.getShow().getId().equals(show.getId())) {
                throw new RuntimeException("Seat does not belong to this show");
            }

            if (seat.isBooked()) {
                throw new RuntimeException(
                    "Seat " + seat.getSeatNumber() + " already booked"
                );
            }

            seat.setBooked(true);
        }

        seatRepository.saveAll(seats);

        double total =
                show.getTicketPrice() * seats.size();

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setShow(show);
        booking.setSeats(seats);
        booking.setTotalAmount(total);
        booking.setStatus("CONFIRMED");
        booking.setBookingTime(LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }
}