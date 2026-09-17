package com.athul.documind.DTO;

import com.athul.documind.Enum.PolicyStatus;
import com.athul.documind.Enum.PolicyType;
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
    private Long clientId;
    private String clientName;
    private String provider;
    private BigDecimal premium;
    private LocalDate startDate;
    private LocalDate endDate;
    private PolicyType policyType;
    private PolicyStatus status;
}