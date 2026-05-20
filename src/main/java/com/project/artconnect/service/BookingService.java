package com.project.artconnect.service;

import com.project.artconnect.model.Booking;
import com.project.artconnect.model.Workshop;

import java.util.List;

public interface BookingService {
    List<Booking> findByWorkshop(Workshop workshop);
    List<Booking> getAllBookings();
    void createBooking(Booking booking);
    void updateBooking(Booking booking);
    void deleteBooking(Long bookingId);
}

