package com.athul.bhaang.DTO;

import com.athul.bhaang.Enum.PolicyType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PolicyResponseDTO {
    private Long id;
    private String policyNumber;
    private String insuredName;
    private String clientName;
    private String provider;
    private BigDecimal premium;
    private LocalDate startDate;
    private LocalDate endDate;
    private PolicyType policyType;  // Added this
}