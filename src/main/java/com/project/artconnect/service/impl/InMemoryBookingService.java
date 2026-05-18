package com.project.artconnect.service.impl;

import com.project.artconnect.model.Booking;
import com.project.artconnect.service.BookingService;

import java.util.ArrayList;
import java.util.List;

public class InMemoryBookingService implements BookingService {
    private final List<Booking> list = new ArrayList<>();

    @Override
    public List<Booking> findByWorkshopTitle(String workshopTitle) {
        List<Booking> res = new ArrayList<>();
        for (Booking b : list) {
            if (b.getWorkshop() != null && workshopTitle.equals(b.getWorkshop().getTitle())) res.add(b);
        }
        return res;
    }

    @Override
    public void createBooking(Booking booking) {
        list.add(booking);
    }
}

