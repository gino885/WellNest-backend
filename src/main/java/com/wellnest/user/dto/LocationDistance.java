package com.wellnest.user.dto;

import com.wellnest.user.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDistance {
    private String name;
    private String phone;
    private String location;
    private String distance;
}


