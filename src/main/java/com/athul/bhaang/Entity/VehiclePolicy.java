package com.athul.bhaang.Entity;

import com.athul.bhaang.Enum.PolicyStatus;
import com.athul.bhaang.Enum.PolicyType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter

@AllArgsConstructor
@Entity
public class VehiclePolicy extends Policy {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = true)
    private Vehicle vehicle;

    private String vehicleRegistration;
    private String vehicleType;
    private String coverageType;


    public VehiclePolicy() {
        super();

        this.setStatus(PolicyStatus.ACTIVE);
    }
}