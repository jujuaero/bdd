package com.project.artconnect.service.impl;

import com.project.artconnect.model.Booking;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.BookingService;

import java.util.ArrayList;
import java.util.List;

public class InMemoryBookingService implements BookingService {
    private final List<Booking> list = new ArrayList<>();

    @Override
    public List<Booking> findByWorkshop(Workshop workshop) {
        List<Booking> res = new ArrayList<>();
        if (workshop == null) return res;
        for (Booking b : list) {
            if (b.getWorkshop() != null && workshop.getId() != null && workshop.getId().equals(b.getWorkshop().getId())) res.add(b);
        }
        return res;
    }

    @Override
    public void createBooking(Booking booking) {
        list.add(booking);
    }
}

