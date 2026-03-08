package com.athul.bhaang.Mapper;

import com.athul.bhaang.DTO.PolicyResponseDTO;
import com.athul.bhaang.Entity.Policy;

import java.util.List;
import java.util.stream.Collectors;

public class PolicyMapper {

    public static PolicyResponseDTO toDTO(Policy p) {
        return PolicyResponseDTO.builder()
                .id(p.getPolicyId())
                .policyNumber(p.getPolicyNumber())
                .insuredName(p.getInsuredName())
                .clientName(p.getClient() != null ? p.getClient().getName() : null)
                .provider(p.getProvider())
                .premium(p.getPremium())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .policyType(p.getType())  // Now this works!
                .build();
    }

    public static List<PolicyResponseDTO> toDTOList(List<Policy> policies) {
        return policies.stream()
                .map(PolicyMapper::toDTO)
                .collect(Collectors.toList());
    }
}