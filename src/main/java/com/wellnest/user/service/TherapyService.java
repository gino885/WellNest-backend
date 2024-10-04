package com.wellnest.user.service;

import com.wellnest.user.dto.LocationDistance;
import com.wellnest.user.model.Location;

import java.util.List;

public interface TherapyService {
    List<LocationDistance> getCoordinates(Location userLocation) throws Exception;
}