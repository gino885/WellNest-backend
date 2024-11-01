package com.wellnest.user.controller;

import com.wellnest.user.dto.LocationDistance;
import com.wellnest.user.dto.UserRegisterRequest;
import com.wellnest.user.model.Appointment;
import com.wellnest.user.model.Location;
import com.wellnest.user.service.TherapyService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Key;
import java.util.Base64;
import java.util.List;

@RestController
public class TherapyController {

    @Autowired
    TherapyService therapyService;
    String secretKey = System.getenv("JWT_SECRET_KEY");
    private Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));

    @PostMapping("/location")
    public List<LocationDistance> getTherapyCenter(@RequestBody Location location) throws Exception{
        return therapyService.getCoordinates(location);
    }

    @GetMapping("/report")
    public void generateReport(@RequestHeader("Authorization") String authToken) throws Exception{
        if (authToken != null && authToken.startsWith("Bearer ")) {
            String token = authToken.substring(7);
            String userId = getUserIdFromToken(token);

            therapyService.generateReport(Integer.parseInt(userId));
        }

    }

    @GetMapping("/appointment")
    public List<Appointment> getAppointmentDetail() throws Exception{
        return  therapyService.getAppointmentDetail();
    }
    private String getUserIdFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}
