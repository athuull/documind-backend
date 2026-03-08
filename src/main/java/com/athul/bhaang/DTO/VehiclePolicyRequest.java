package com.athul.bhaang.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VehiclePolicyRequest(

        @NotBlank
        String policyNumber,

        @NotBlank
        String insuredName,

        @NotBlank
        String provider,

        @NotNull
        @Positive
        BigDecimal premium,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @NotNull
        Long vehicleId
) {}