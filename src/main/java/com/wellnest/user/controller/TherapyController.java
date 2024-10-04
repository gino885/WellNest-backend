package com.wellnest.user.controller;

import com.wellnest.user.dto.LocationDistance;
import com.wellnest.user.dto.UserRegisterRequest;
import com.wellnest.user.model.Location;
import com.wellnest.user.service.TherapyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
public class TherapyController {

    @Autowired
    TherapyService therapyService;


    @PostMapping("/location")
    public List<LocationDistance> getTherapyCenter(@RequestBody Location location) throws Exception{
        return therapyService.getCoordinates(location);
    }

}
