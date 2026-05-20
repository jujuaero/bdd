package com.project.artconnect.dao;

import com.project.artconnect.model.Booking;
import com.project.artconnect.model.Workshop;
import java.util.List;

public interface BookingDao {
    List<Booking> findByWorkshop(Workshop workshop);
    List<Booking> findAll();
    void save(Booking booking);
    void update(Booking booking);
    void delete(Long id);
}



