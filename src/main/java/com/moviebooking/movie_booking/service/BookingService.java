package com.moviebooking.movie_booking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moviebooking.movie_booking.dto.BookingRequest;
import com.moviebooking.movie_booking.model.Booking;
import com.moviebooking.movie_booking.model.Seat;
import com.moviebooking.movie_booking.model.Show;
import com.moviebooking.movie_booking.model.User;
import com.moviebooking.movie_booking.repository.BookingRepository;
import com.moviebooking.movie_booking.repository.SeatRepository;
import com.moviebooking.movie_booking.repository.ShowRepository;
import com.moviebooking.movie_booking.repository.UserRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final TicketPdfService ticketPdfService;
    private final EmailService emailService;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            ShowRepository showRepository,
            SeatRepository seatRepository,
            TicketPdfService ticketPdfService,
            EmailService emailService) {

        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
        this.ticketPdfService = ticketPdfService;
        this.emailService = emailService;
    }

    @Transactional
    public Booking bookSeats(BookingRequest request) {

        if (request.getSeatIds() == null ||
                request.getSeatIds().isEmpty()) {

            throw new RuntimeException(
                    "Please select at least one seat"
            );
        }

        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Show show = showRepository
                .findById(request.getShowId())
                .orElseThrow(() ->
                        new RuntimeException("Show not found"));

        // Check whether show has already started
        LocalDateTime showDateTime =
                LocalDateTime.of(
                        show.getShowDate(),
                        show.getShowTime()
                );

        if (!LocalDateTime.now().isBefore(showDateTime)) {

            throw new RuntimeException(
                    "Cannot book tickets. This show has already started or expired."
            );
        }

        List<Seat> seats =
                seatRepository.findAllById(
                        request.getSeatIds()
                );

        if (seats.size() !=
                request.getSeatIds().size()) {

            throw new RuntimeException(
                    "Invalid seat selected"
            );
        }

        for (Seat seat : seats) {

            if (!seat.getShow()
                    .getId()
                    .equals(show.getId())) {

                throw new RuntimeException(
                        "Seat does not belong to this show"
                );
            }

            if (seat.isBooked()) {

                throw new RuntimeException(
                        "Seat "
                        + seat.getSeatNumber()
                        + " already booked"
                );
            }

            seat.setBooked(true);
        }

        seatRepository.saveAll(seats);

        double total =
                show.getTicketPrice()
                * seats.size();

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setShow(show);
        booking.setSeats(seats);
        booking.setTotalAmount(total);
        booking.setStatus("CONFIRMED");
        booking.setBookingTime(
                LocalDateTime.now()
        );

        Booking savedBooking =
                bookingRepository.save(booking);

        try {

            byte[] ticketPdf =
                    ticketPdfService.generateTicket(
                            savedBooking
                    );

            emailService.sendTicketEmail(
                    user.getEmail(),
                    user.getName(),
                    ticketPdf,
                    savedBooking.getId()
            );

        } catch (Exception e) {

            System.err.println(
                    "Booking successful but ticket email failed: "
                    + e.getMessage()
            );
        }

        return savedBooking;
    }

    public List<Booking> getUserBookings(
            Long userId) {

        return bookingRepository
                .findByUserId(userId);
    }

    public List<Booking> getAllBookings() {

        return bookingRepository.findAll();
    }

    // Runs every 1 minute
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireBookings() {

        List<Booking> bookings =
                bookingRepository
                        .findByStatus("CONFIRMED");

        LocalDateTime now =
                LocalDateTime.now();

        for (Booking booking : bookings) {

            Show show =
                    booking.getShow();

            LocalDateTime showDateTime =
                    LocalDateTime.of(
                            show.getShowDate(),
                            show.getShowTime()
                    );

            if (!now.isBefore(showDateTime)) {

                booking.setStatus("EXPIRED");
            }
        }

        bookingRepository.saveAll(bookings);
    }
    public byte[] downloadTicket(Long bookingId) {

        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found"));

        return ticketPdfService.generateTicket(booking);
    }
}