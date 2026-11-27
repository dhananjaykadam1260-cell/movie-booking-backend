package com.moviebooking.movie_booking.dto;

import java.util.List;

/** The user is taken from the authenticated token, not from the body. */
public class BookingRequest {

    private Long showId;
    private List<Long> seatIds;

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Long> seatIds) {
        this.seatIds = seatIds;
    }
}
