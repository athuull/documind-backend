package com.athul.documind.DTO;

import com.athul.documind.Enum.PolicyType;
import lombok.Data;

@Data
public class PolicyExtractionDTO {
    private String policyNumber;
    private String insuredName;
    private String insurerName;
    private Double premiumAmount;
    private String effectiveDate;
    private String expiryDate;
    private PolicyType policyType;
    private String rawText;
    private Double confidenceScore;

    // Vehicle-specific
    private String vehicleRegistration;
    private String vehicleType;

    // Common
    private String coverageType;

    // Health-specific
    private Double coverageAmount;
    private String beneficiaryName;
    private Integer insuredPersonAge;  // Added this

}