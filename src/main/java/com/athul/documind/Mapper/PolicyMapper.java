package com.athul.documind.Mapper;

import com.athul.documind.DTO.PolicyResponseDTO;
import com.athul.documind.Entity.Policy;

import java.util.List;
import java.util.stream.Collectors;

public class PolicyMapper {

    public static PolicyResponseDTO toDTO(Policy p) {
        return PolicyResponseDTO.builder()
                .id(p.getPolicyId())
                .policyNumber(p.getPolicyNumber())
                .insuredName(p.getInsuredName())
                .clientId(p.getClient() != null ? p.getClient().getId() : null)
                .clientName(p.getClient() != null ? p.getClient().getName() : null)
                .provider(p.getProvider())
                .premium(p.getPremium())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .policyType(p.getType())
                .status(p.getStatus())
                .build();
    }

    public static List<PolicyResponseDTO> toDTOList(List<Policy> policies) {
        return policies.stream()
                .map(PolicyMapper::toDTO)
                .collect(Collectors.toList());
    }
}