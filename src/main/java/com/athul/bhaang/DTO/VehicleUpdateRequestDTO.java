package com.athul.bhaang.DTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VehicleUpdateRequestDTO {
    private Long id;
    private String registrationNumber;
    private String make;
    private String model;
    private Long clientId;
    private Long version; // Added for Optimistic Locking
}