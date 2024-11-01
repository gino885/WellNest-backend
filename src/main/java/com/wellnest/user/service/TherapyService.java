package com.wellnest.user.service;

import com.wellnest.user.dto.LocationDistance;
import com.wellnest.user.model.Appointment;
import com.wellnest.user.model.Location;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.util.List;

public interface TherapyService {
    List<LocationDistance> getCoordinates(Location userLocation) throws Exception;
    void generateReport (Integer userId) throws Exception;

    List<Appointment> getAppointmentDetail() throws Exception;
}