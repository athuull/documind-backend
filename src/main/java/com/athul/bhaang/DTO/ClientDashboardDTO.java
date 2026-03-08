package com.athul.bhaang.DTO;

import com.athul.bhaang.Enum.PolicyStatus;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ClientDashboardDTO {
    private String clientName;
    private long totalVehicles;
    private List<VehicleSummary> vehicles;

    @Data
    @Builder
    public static class VehicleSummary {
        private String model;
        private String regNumber;
        private String activePolicyNumber;
        private PolicyStatus currentStatus;
    }
}