package com.athul.bhaang.Entity;

import com.athul.bhaang.Enum.HealthPolicyType;
import com.athul.bhaang.Enum.PolicyStatus;
import com.athul.bhaang.Enum.PolicyType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Entity
public class HealthPolicy extends Policy {

    @Column(nullable = false)
    private Integer insuredPersonAge;

    private String coverageType;
    private Double coverageAmount;
    private String beneficiaryName;

    @Enumerated(EnumType.STRING)
    @Column(name = "health_policy_type")
    private HealthPolicyType healthPolicyType;

    public HealthPolicy() {
        super();
        this.setType(PolicyType.HEALTH);
        this.setStatus(PolicyStatus.ACTIVE);
    }
}