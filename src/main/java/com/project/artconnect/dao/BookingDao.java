package com.project.artconnect.dao;

import com.project.artconnect.model.Booking;
import com.project.artconnect.model.Workshop;
import java.util.List;

public interface BookingDao {
    List<Booking> findByWorkshop(Workshop workshop);
    void save(Booking booking);
}

