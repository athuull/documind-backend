package com.athul.bhaang.DTO;

import com.athul.bhaang.Enum.VehiclePolicyType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VehiclePolicyResponse(

        Long policyId,
        String policyNumber,
        String provider,
        String insuredName,
        BigDecimal premium,
        LocalDate startDate,
        LocalDate endDate,
        Long vehicleId,
        String vehicleRegistrationNumber



) {}