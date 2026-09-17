package com.athul.documind.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class VehicleResponseDTO {
    private Long id;
    private String registrationNumber;
    private String make;
    private String model;
    private Integer year;
    private Long clientId;
    private String clientName; // Useful to show who the client is
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}