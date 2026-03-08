package com.athul.bhaang.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PolicyCreateRequestDTO {

    private String policyNumber;
    private String provider;
    private String type;
    private Long vehicleId;
}