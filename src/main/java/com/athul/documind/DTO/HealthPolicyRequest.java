package com.athul.documind.DTO;

import com.athul.documind.Enum.HealthPolicyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HealthPolicyRequest(
        @NotBlank String policyNumber,
        @NotBlank String insuredName,
        @NotBlank String provider,
        @NotNull BigDecimal premium,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull Long clientId,
        @NotNull Integer insuredPersonAge,
        String coverageType,
        Double coverageAmount,
        String beneficiaryName,
        HealthPolicyType healthPolicyType
) {}