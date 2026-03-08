package com.athul.bhaang.DTO;

import com.athul.bhaang.Enum.PolicyType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyRequestDTO {

    @NotBlank(message = "Policy number is required")
    private String policyNumber;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotBlank(message = "Insured name is required")
    private String insuredName;

    @NotBlank(message = "Provider is required")
    private String provider;

    @NotNull(message = "Premium is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Premium must be greater than 0")
    private BigDecimal premium;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Policy type is required")
    private PolicyType policyType;  // VEHICLE or HEALTH

    // ========== Vehicle Policy Fields ==========
    private String vehicleRegistration;
    private String vehicleType;

    // ========== Health Policy Fields ==========
    private String coverageType;
    private Double coverageAmount;
    private String beneficiaryName;
}