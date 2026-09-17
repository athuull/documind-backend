package com.athul.documind.DTO;

import com.athul.documind.Enum.PolicyStatus;
import com.athul.documind.Enum.PolicyType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ClientDashboardDTO {
    private Long clientId;
    private String clientName;
    private String email;
    private String phone;
    private String assignedToAgent;
    private long totalVehicles;
    private long totalPolicies;
    private long activePoliciesCount;
    private BigDecimal totalAnnualPremium;
    private long totalDocuments;
    private List<VehicleSummary> vehicles;
    private List<PolicySummary> policies;

    @Data
    @Builder
    public static class VehicleSummary {
        private Long vehicleId;
        private String make;
        private String model;
        private Integer year;
        private String regNumber;
        private String activePolicyNumber;
        private PolicyStatus currentStatus;
    }

    @Data
    @Builder
    public static class PolicySummary {
        private Long policyId;
        private String policyNumber;
        private PolicyType policyType;
        private PolicyStatus status;
        private String provider;
        private BigDecimal premium;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean isExpiringSoon;
    }
}