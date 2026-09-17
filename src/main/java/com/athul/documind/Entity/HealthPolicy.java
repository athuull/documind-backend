package com.athul.documind.Entity;

import com.athul.documind.Enum.HealthPolicyType;
import com.athul.documind.Enum.PolicyStatus;
import com.athul.documind.Enum.PolicyType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Entity
public class HealthPolicy extends Policy {

    @Column(nullable = false)
    private Integer insuredPersonAge = 0;

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