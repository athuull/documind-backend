package com.athul.bhaang.DTO;

import com.athul.bhaang.Enum.HealthPolicyType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HealthPolicyResponse(

        Long policyId,
        String policyNumber,
        String provider,
        String insuredName,
        BigDecimal premium,
        LocalDate startDate,
        LocalDate endDate,
        Integer insuredPersonAge,
        HealthPolicyType type

) {}