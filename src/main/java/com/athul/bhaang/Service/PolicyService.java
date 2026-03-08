package com.athul.bhaang.Service;

import com.athul.bhaang.DTO.PolicyRequestDTO;
import com.athul.bhaang.DTO.PolicyResponseDTO;
import com.athul.bhaang.Entity.Policy;
import com.athul.bhaang.Entity.VehiclePolicy;
import com.athul.bhaang.Entity.HealthPolicy;
import com.athul.bhaang.Entity.Client;
import com.athul.bhaang.Enum.PolicyType;
import com.athul.bhaang.Exception.ClientNotFoundException;
import com.athul.bhaang.Repository.PolicyRepository;
import com.athul.bhaang.Repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public PolicyResponseDTO create(PolicyRequestDTO dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + dto.getClientId()));

        Policy policy = createPolicyByType(dto);

        // Set common fields
        policy.setPolicyNumber(dto.getPolicyNumber());
        policy.setInsuredName(dto.getInsuredName());
        policy.setProvider(dto.getProvider());
        policy.setPremium(dto.getPremium());
        policy.setStartDate(dto.getStartDate());
        policy.setEndDate(dto.getEndDate());
        policy.setClient(client);
        policy.setType(dto.getPolicyType()); // Set the type field

        return mapToDTO(policyRepository.save(policy));
    }

    @Transactional
    public PolicyResponseDTO update(Long id, PolicyRequestDTO dto) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + id));

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + dto.getClientId()));

        // Update type-specific fields
        updatePolicySpecificFields(policy, dto);

        // Update common fields
        policy.setPolicyNumber(dto.getPolicyNumber());
        policy.setInsuredName(dto.getInsuredName());
        policy.setProvider(dto.getProvider());
        policy.setPremium(dto.getPremium());
        policy.setStartDate(dto.getStartDate());
        policy.setEndDate(dto.getEndDate());
        policy.setClient(client);
        policy.setType(dto.getPolicyType()); // Update type

        return mapToDTO(policyRepository.save(policy));
    }

    @Transactional
    public void delete(Long id) {
        if (!policyRepository.existsById(id)) {
            throw new RuntimeException("Policy not found with id: " + id);
        }
        policyRepository.deleteById(id);
    }

    public PolicyResponseDTO getById(Long id) {
        return policyRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + id));
    }

    public List<PolicyResponseDTO> getAll() {
        return policyRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PolicyResponseDTO> getByClientId(Long clientId) {
        return policyRepository.findByClientId(clientId)
                .stream()
                .filter(p -> p.getClient() != null && p.getClient().getId().equals(clientId))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public PolicyResponseDTO getByPolicyNumber(String policyNumber) {
        return policyRepository.findByPolicyNumber(policyNumber)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Policy not found with number: " + policyNumber));
    }

    public List<PolicyResponseDTO> getByType(PolicyType type) {
        return policyRepository.findAll()
                .stream()
                .filter(p -> p.getType() == type)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Helper method to create policy by type
    private Policy createPolicyByType(PolicyRequestDTO dto) {
        PolicyType policyType = dto.getPolicyType();

        if (policyType == null) {
            throw new IllegalArgumentException("Policy type is required");
        }

        return switch (policyType) {
            case VEHICLE -> {
                VehiclePolicy vehiclePolicy = new VehiclePolicy();
                vehiclePolicy.setVehicleRegistration(dto.getVehicleRegistration());
                vehiclePolicy.setVehicleType(dto.getVehicleType());
                vehiclePolicy.setCoverageType(dto.getCoverageType());
                yield vehiclePolicy;
            }
            case HEALTH -> {
                HealthPolicy healthPolicy = new HealthPolicy();
                healthPolicy.setCoverageType(dto.getCoverageType());
                healthPolicy.setCoverageAmount(dto.getCoverageAmount());
                healthPolicy.setBeneficiaryName(dto.getBeneficiaryName());
                yield healthPolicy;
            }
            default -> throw new IllegalArgumentException("Unsupported policy type: " + policyType);
        };
    }

    // Helper method to update policy-specific fields
    private void updatePolicySpecificFields(Policy policy, PolicyRequestDTO dto) {
        if (policy instanceof VehiclePolicy vehiclePolicy) {
            vehiclePolicy.setVehicleRegistration(dto.getVehicleRegistration());
            vehiclePolicy.setVehicleType(dto.getVehicleType());
            vehiclePolicy.setCoverageType(dto.getCoverageType());
        } else if (policy instanceof HealthPolicy healthPolicy) {
            healthPolicy.setCoverageType(dto.getCoverageType());
            healthPolicy.setCoverageAmount(dto.getCoverageAmount());
            healthPolicy.setBeneficiaryName(dto.getBeneficiaryName());
        }
    }

    private PolicyResponseDTO mapToDTO(Policy policy) {
        return PolicyResponseDTO.builder()
                .id(policy.getPolicyId())
                .policyNumber(policy.getPolicyNumber())
                .insuredName(policy.getInsuredName())
                .clientName(policy.getClient() != null ? policy.getClient().getName() : null)
                .provider(policy.getProvider())
                .premium(policy.getPremium())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .policyType(policy.getType())  // Now uses the persisted type field
                .build();
    }
}