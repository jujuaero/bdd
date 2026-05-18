package com.project.artconnect.service;

import com.project.artconnect.model.Booking;

import java.util.List;

public interface BookingService {
    List<Booking> findByWorkshopTitle(String workshopTitle);
    void createBooking(Booking booking);
}

