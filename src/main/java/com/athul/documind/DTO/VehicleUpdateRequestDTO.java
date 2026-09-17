package com.athul.documind.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VehicleUpdateRequestDTO {
    private Long id;
    private String registrationNumber;
    private String make;
    private String model;
    private Integer year;
    private Long clientId;
    @NotNull(message = "Version is required for optimistic locking")
    private Long version;
}