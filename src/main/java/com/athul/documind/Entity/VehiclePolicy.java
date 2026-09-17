package com.athul.documind.Entity;

import com.athul.documind.Enum.PolicyStatus;
import com.athul.documind.Enum.PolicyType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter

@AllArgsConstructor
@Entity
public class VehiclePolicy extends Policy {

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "vehicle_id", nullable = true)
    private Vehicle vehicle;

    private String vehicleRegistration;
    private String vehicleType;
    private String coverageType;


    public VehiclePolicy() {
        super();
        this.setType(PolicyType.VEHICLE);
        this.setStatus(PolicyStatus.ACTIVE);
    }
}