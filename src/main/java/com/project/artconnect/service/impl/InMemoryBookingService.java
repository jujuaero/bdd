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
    public List<Booking> getAllBookings() {
        return new ArrayList<>(list);
    }

    @Override
    public void createBooking(Booking booking) {
        list.add(booking);
    }

    @Override
    public void updateBooking(Booking booking) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() != null && list.get(i).getId().equals(booking.getId())) {
                list.set(i, booking);
                return;
            }
        }
    }

    @Override
    public void deleteBooking(Long bookingId) {
        list.removeIf(b -> b.getId() != null && b.getId().equals(bookingId));
    }
}

