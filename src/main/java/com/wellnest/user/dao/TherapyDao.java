package com.wellnest.user.dao;

import com.github.houbb.heaven.annotation.reflect.Param;
import com.wellnest.user.model.Location;
import com.wellnest.user.model.TherapyCenter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TherapyDao extends CrudRepository<TherapyCenter, Double> {

    @Query(value = "SELECT name, location, phone, (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLng)) + sin(radians(:userLat)) * sin(radians(latitude)))) AS distance " +
            "FROM therapy_center ORDER BY distance ASC LIMIT 5", nativeQuery = true)
    List<Object[]> findNearestLocations(@Param("userLat") Double userLat, @Param("userLng") Double userLng);
}
