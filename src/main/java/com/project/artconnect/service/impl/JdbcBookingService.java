package com.project.artconnect.service.impl;

import com.project.artconnect.model.Booking;
import com.project.artconnect.persistence.JdbcBookingDao;
import com.project.artconnect.service.BookingService;

import java.util.List;

public class JdbcBookingService implements BookingService {
    private final JdbcBookingDao dao = new JdbcBookingDao();

    @Override
    public List<Booking> findByWorkshopTitle(String workshopTitle) {
        return dao.findByWorkshopTitle(workshopTitle);
    }

    @Override
    public void createBooking(Booking booking) {
        dao.save(booking);
    }
}

