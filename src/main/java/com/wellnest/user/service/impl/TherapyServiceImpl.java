package com.wellnest.user.service.impl;

import com.wellnest.user.dao.TherapyDao;
import com.wellnest.user.dto.LocationDistance;
import com.wellnest.user.model.Location;
import com.wellnest.user.service.TherapyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TherapyServiceImpl implements TherapyService {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired
    private TherapyDao therapyDao;
    @Override
    public List<LocationDistance> getCoordinates(Location userLocation) throws Exception {
        double userLat = userLocation.getLatitude();
        double userLng = userLocation.getLongitude();

        List<Object[]> nearestLocations = therapyDao.findNearestLocations(userLat, userLng);
        List<LocationDistance> results = new ArrayList<>();

        for (Object[] row : nearestLocations) {
            String name = (row[0]).toString();
            String address = ( row[1]).toString();
            String phone = ( row[2]).toString();
            double distance = ((Number) row[3]).doubleValue();

            results.add(new LocationDistance(name, address, phone, String.format("%.2f km", distance)));
        }

        return results;
    }
}