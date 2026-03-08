package com.athul.bhaang.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleCreateRequestDTO {
    private String registrationNumber;
    private String make;
    private String model;
    private Long clientId;
}