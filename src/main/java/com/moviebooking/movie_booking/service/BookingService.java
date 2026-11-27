package com.moviebooking.movie_booking.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moviebooking.movie_booking.dto.BookingRequest;
import com.moviebooking.movie_booking.exception.ApiException;
import com.moviebooking.movie_booking.model.Booking;
import com.moviebooking.movie_booking.model.Seat;
import com.moviebooking.movie_booking.model.Show;
import com.moviebooking.movie_booking.model.User;
import com.moviebooking.movie_booking.repository.BookingRepository;
import com.moviebooking.movie_booking.repository.SeatRepository;
import com.moviebooking.movie_booking.repository.ShowRepository;
import com.moviebooking.movie_booking.repository.UserRepository;
import com.moviebooking.movie_booking.security.AuthUser;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

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
    public Booking bookSeats(AuthUser actor, BookingRequest request) {

        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Please select at least one seat.");
        }

        if (request.getShowId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Show is required.");
        }

        User user = userRepository.findById(actor.id())
                .orElseThrow(() ->
                        new ApiException(HttpStatus.UNAUTHORIZED, "User not found."));

        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "Show not found."));

        LocalDateTime showDateTime = LocalDateTime.of(show.getShowDate(), show.getShowTime());

        if (!LocalDateTime.now().isBefore(showDateTime)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Cannot book tickets. This show has already started or expired.");
        }

        // Duplicate IDs in the request would otherwise fail the size check.
        List<Long> seatIds = request.getSeatIds().stream().distinct().toList();

        // Locks the seat rows; other bookings for these seats wait here.
        List<Seat> seats = seatRepository.findAllByIdForUpdate(seatIds);

        if (seats.size() != seatIds.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid seat selected.");
        }

        for (Seat seat : seats) {

            if (!seat.getShow().getId().equals(show.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Seat does not belong to this show.");
            }

            if (seat.isBooked()) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Seat " + seat.getSeatNumber() + " was just booked by someone else.");
            }

            seat.setBooked(true);
        }

        double total = show.getTicketPrice() * seats.size();

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShow(show);
        booking.setSeats(seats);
        booking.setTotalAmount(total);
        booking.setStatus("CONFIRMED");
        booking.setBookingTime(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        try {
            byte[] ticketPdf = ticketPdfService.generateTicket(savedBooking);

            emailService.sendTicketEmail(
                    user.getEmail(),
                    user.getName(),
                    ticketPdf,
                    savedBooking.getId());

        } catch (Exception e) {
            // The booking is committed; a failed email must not undo it.
            log.warn("Booking {} saved but ticket email failed: {}",
                    savedBooking.getId(), e.getMessage());
        }

        return savedBooking;
    }

    /** Own bookings only, unless the caller is an admin. */
    public List<Booking> getUserBookings(AuthUser actor, Long userId) {

        if (!actor.isAdmin() && !actor.id().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "You can only view your own bookings.");
        }

        return bookingRepository.findByUserId(userId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Returns the booking if the caller owns it or is an admin.
     * Non-owners get "not found" so booking IDs cannot be probed.
     */
    private Booking findAccessibleBooking(AuthUser actor, Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "Booking not found."));

        boolean owner = booking.getUser() != null
                && actor.id().equals(booking.getUser().getId());

        if (!owner && !actor.isAdmin()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Booking not found.");
        }

        return booking;
    }

    public byte[] downloadTicket(AuthUser actor, Long bookingId) {

        Booking booking = findAccessibleBooking(actor, bookingId);

        return ticketPdfService.generateTicket(booking);
    }

    /** Cancels a confirmed booking before the show starts and frees its seats. */
    @Transactional
    public Booking cancelBooking(AuthUser actor, Long bookingId) {

        Booking booking = findAccessibleBooking(actor, bookingId);

        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Only confirmed bookings can be cancelled.");
        }

        Show show = booking.getShow();
        LocalDateTime showDateTime = LocalDateTime.of(show.getShowDate(), show.getShowTime());

        if (!LocalDateTime.now().isBefore(showDateTime)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "This show has already started and cannot be cancelled.");
        }

        // Lock the seats again before freeing them.
        List<Long> seatIds = booking.getSeats().stream()
                .map(Seat::getId)
                .collect(Collectors.toList());

        for (Seat seat : seatRepository.findAllByIdForUpdate(seatIds)) {
            seat.setBooked(false);
        }

        booking.setStatus("CANCELLED");

        return bookingRepository.save(booking);
    }

    /**
     * Marks confirmed bookings as EXPIRED once their show has started.
     * Only shows dated today or earlier are loaded, not every booking.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireBookings() {

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings =
                bookingRepository.findConfirmedShowsOnOrBefore(now.toLocalDate());

        for (Booking booking : bookings) {

            Show show = booking.getShow();

            LocalDateTime showDateTime =
                    LocalDateTime.of(show.getShowDate(), show.getShowTime());

            if (!now.isBefore(showDateTime)) {
                booking.setStatus("EXPIRED");
            }
        }

        bookingRepository.saveAll(bookings);
    }
}
