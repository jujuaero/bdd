package com.project.artconnect.service;

import com.project.artconnect.model.Booking;
import com.project.artconnect.model.Workshop;

import java.util.List;

public interface BookingService {
    List<Booking> findByWorkshop(Workshop workshop);
    void createBooking(Booking booking);
}

