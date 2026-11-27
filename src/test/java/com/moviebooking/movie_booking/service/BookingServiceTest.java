package com.moviebooking.movie_booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

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

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock BookingRepository bookingRepository;
    @Mock UserRepository userRepository;
    @Mock ShowRepository showRepository;
    @Mock SeatRepository seatRepository;
    @Mock TicketPdfService ticketPdfService;
    @Mock EmailService emailService;

    @InjectMocks BookingService bookingService;

    private static Show show(LocalDate date) {
        Show show = new Show();
        show.setId(10L);
        show.setShowDate(date);
        show.setShowTime(LocalTime.of(18, 0));
        show.setTicketPrice(200);
        return show;
    }

    private static Show futureShow() {
        return show(LocalDate.now().plusDays(1));
    }

    private static Seat seat(long id, String number, Show show, boolean booked) {
        Seat seat = new Seat();
        seat.setId(id);
        seat.setSeatNumber(number);
        seat.setShow(show);
        seat.setBooked(booked);
        return seat;
    }

    private static User user(long id) {
        User user = new User();
        user.setId(id);
        user.setName("Ann");
        user.setEmail("ann@example.com");
        user.setRole("USER");
        return user;
    }

    private static BookingRequest request(long showId, Long... seatIds) {
        BookingRequest request = new BookingRequest();
        request.setShowId(showId);
        request.setSeatIds(List.of(seatIds));
        return request;
    }

    @Test
    void bookingMarksSeatsAndUsesAuthenticatedUser() {
        Show show = futureShow();
        List<Seat> seats = List.of(seat(1L, "A1", show, false), seat(2L, "A2", show, false));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(showRepository.findById(10L)).thenReturn(Optional.of(show));
        when(seatRepository.findAllByIdForUpdate(anyCollection())).thenReturn(seats);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Booking booking = bookingService.bookSeats(
                new AuthUser(1L, "ann@example.com", "USER"), request(10L, 1L, 2L));

        assertEquals("CONFIRMED", booking.getStatus());
        assertEquals(400.0, booking.getTotalAmount());
        assertEquals(1L, booking.getUser().getId());
        assertTrue(seats.stream().allMatch(Seat::isBooked));
    }

    @Test
    void duplicateSeatIdsAreTreatedAsOne() {
        Show show = futureShow();
        List<Seat> seats = List.of(seat(1L, "A1", show, false));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(showRepository.findById(10L)).thenReturn(Optional.of(show));
        when(seatRepository.findAllByIdForUpdate(List.of(1L))).thenReturn(seats);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Booking booking = bookingService.bookSeats(
                new AuthUser(1L, "ann@example.com", "USER"), request(10L, 1L, 1L));

        assertEquals(200.0, booking.getTotalAmount());
    }

    @Test
    void alreadyBookedSeatIsConflict() {
        Show show = futureShow();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(showRepository.findById(10L)).thenReturn(Optional.of(show));
        when(seatRepository.findAllByIdForUpdate(anyCollection()))
                .thenReturn(List.of(seat(1L, "A1", show, false), seat(2L, "A2", show, true)));

        ApiException ex = assertThrows(ApiException.class, () ->
                bookingService.bookSeats(
                        new AuthUser(1L, "ann@example.com", "USER"), request(10L, 1L, 2L)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertTrue(ex.getMessage().contains("A2"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void startedShowIsRejectedBeforeLockingSeats() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(showRepository.findById(10L)).thenReturn(Optional.of(show(LocalDate.now().minusDays(1))));

        ApiException ex = assertThrows(ApiException.class, () ->
                bookingService.bookSeats(
                        new AuthUser(1L, "ann@example.com", "USER"), request(10L, 1L)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verifyNoInteractions(seatRepository);
    }

    @Test
    void emailFailureDoesNotUndoBooking() {
        Show show = futureShow();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(showRepository.findById(10L)).thenReturn(Optional.of(show));
        when(seatRepository.findAllByIdForUpdate(anyCollection()))
                .thenReturn(List.of(seat(1L, "A1", show, false)));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("smtp down"))
                .when(emailService).sendTicketEmail(any(), any(), any(), any());

        Booking booking = bookingService.bookSeats(
                new AuthUser(1L, "ann@example.com", "USER"), request(10L, 1L));

        assertEquals("CONFIRMED", booking.getStatus());
    }

    @Test
    void userCannotListAnotherUsersBookings() {
        assertThrows(ApiException.class, () ->
                bookingService.getUserBookings(
                        new AuthUser(2L, "bob@example.com", "USER"), 1L));

        verifyNoInteractions(bookingRepository);
    }

    @Test
    void adminCanListAnyUsersBookings() {
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of());

        bookingService.getUserBookings(new AuthUser(99L, "admin@example.com", "ADMIN"), 1L);

        verify(bookingRepository).findByUserId(1L);
    }

    @Test
    void nonOwnerTicketRequestLooksLikeNotFound() {
        Booking booking = new Booking();
        booking.setUser(user(1L));
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        ApiException ex = assertThrows(ApiException.class, () ->
                bookingService.downloadTicket(
                        new AuthUser(2L, "bob@example.com", "USER"), 5L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verifyNoInteractions(ticketPdfService);
    }

    @Test
    void cancellingReleasesSeats() {
        Show show = futureShow();
        Seat seat = seat(1L, "A1", show, true);

        Booking booking = new Booking();
        booking.setUser(user(1L));
        booking.setShow(show);
        booking.setSeats(List.of(seat));
        booking.setStatus("CONFIRMED");

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(seatRepository.findAllByIdForUpdate(anyCollection())).thenReturn(List.of(seat));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Booking cancelled = bookingService.cancelBooking(
                new AuthUser(1L, "ann@example.com", "USER"), 5L);

        assertEquals("CANCELLED", cancelled.getStatus());
        assertFalse(seat.isBooked());
    }
}
