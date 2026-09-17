package com.athul.documind.Service;

import com.athul.documind.DTO.PolicyRequestDTO;
import com.athul.documind.DTO.PolicyResponseDTO;
import com.athul.documind.Entity.Client;
import com.athul.documind.Entity.HealthPolicy;
import com.athul.documind.Entity.Policy;
import com.athul.documind.Entity.VehiclePolicy;
import com.athul.documind.Enum.PolicyStatus;
import com.athul.documind.Enum.PolicyType;
import com.athul.documind.Exception.ClientNotFoundException;
import com.athul.documind.Exception.PolicyNotFoundException;
import com.athul.documind.Mapper.PolicyMapper;
import com.athul.documind.Repository.ClientRepository;
import com.athul.documind.Repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public PolicyResponseDTO create(PolicyRequestDTO dto) {
        if (policyRepository.existsByPolicyNumber(dto.getPolicyNumber())) {
            throw new IllegalArgumentException("Policy with number " + dto.getPolicyNumber() + " already exists");
        }

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

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
        policy.setType(dto.getPolicyType());
        policy.setStatus(PolicyStatus.ACTIVE);

        return PolicyMapper.toDTO(policyRepository.save(policy));
    }

    @Transactional
    public PolicyResponseDTO update(Long id, PolicyRequestDTO dto) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with id: " + id));

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + dto.getClientId()));

        // Check uniqueness if policy number changed
        if (!policy.getPolicyNumber().equals(dto.getPolicyNumber()) &&
                policyRepository.existsByPolicyNumber(dto.getPolicyNumber())) {
            throw new IllegalArgumentException("Policy number " + dto.getPolicyNumber() + " is already in use");
        }

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
        policy.setType(dto.getPolicyType());

        return PolicyMapper.toDTO(policyRepository.save(policy));
    }

    @Transactional
    public PolicyResponseDTO updateStatus(Long id, PolicyStatus newStatus) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with id: " + id));

        policy.setStatus(newStatus);
        return PolicyMapper.toDTO(policyRepository.save(policy));
    }

    @Transactional
    public void delete(Long id) {
        if (!policyRepository.existsById(id)) {
            throw new PolicyNotFoundException("Policy not found with id: " + id);
        }
        policyRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PolicyResponseDTO getById(Long id) {
        return policyRepository.findById(id)
                .map(PolicyMapper::toDTO)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<PolicyResponseDTO> getAll() {
        return policyRepository.findAll()
                .stream()
                .map(PolicyMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PolicyResponseDTO> getByClientId(Long clientId) {
        return policyRepository.findByClientId(clientId)
                .stream()
                .map(PolicyMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PolicyResponseDTO getByPolicyNumber(String policyNumber) {
        return policyRepository.findByPolicyNumber(policyNumber)
                .map(PolicyMapper::toDTO)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with number: " + policyNumber));
    }

    @Transactional(readOnly = true)
    public List<PolicyResponseDTO> getByType(PolicyType type) {
        return policyRepository.findByType(type)
                .stream()
                .map(PolicyMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PolicyResponseDTO> getByStatus(PolicyStatus status) {
        return policyRepository.findByStatus(status)
                .stream()
                .map(PolicyMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PolicyResponseDTO> getExpiringPolicies(int days) {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(days);
        return policyRepository.findByStatusAndEndDateBetween(PolicyStatus.ACTIVE, today, maxDate)
                .stream()
                .map(PolicyMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PolicyResponseDTO> searchPolicies(String query) {
        if (query == null || query.isBlank()) {
            return getAll();
        }
        return policyRepository.searchPolicies(query.trim())
                .stream()
                .map(PolicyMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAgencyStats() {
        List<Policy> all = policyRepository.findAll();
        long totalPolicies = all.size();
        long activeCount = all.stream().filter(p -> p.getStatus() == PolicyStatus.ACTIVE).count();
        long reviewCount = all.stream().filter(p -> p.getStatus() == PolicyStatus.MANUAL_REVIEW).count();
        long expiredCount = all.stream().filter(p -> p.getStatus() == PolicyStatus.EXPIRED).count();
        long cancelledCount = all.stream().filter(p -> p.getStatus() == PolicyStatus.CANCELLED).count();

        BigDecimal totalActivePremium = all.stream()
                .filter(p -> p.getStatus() == PolicyStatus.ACTIVE && p.getPremium() != null)
                .map(Policy::getPremium)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPolicies", totalPolicies);
        stats.put("activePolicies", activeCount);
        stats.put("pendingReviewPolicies", reviewCount);
        stats.put("expiredPolicies", expiredCount);
        stats.put("cancelledPolicies", cancelledCount);
        stats.put("totalActivePremiumVolume", totalActivePremium);

        return stats;
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
                healthPolicy.setInsuredPersonAge(dto.getInsuredPersonAge() != null ? dto.getInsuredPersonAge() : 0);
                yield healthPolicy;
            }
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
            if (dto.getInsuredPersonAge() != null) {
                healthPolicy.setInsuredPersonAge(dto.getInsuredPersonAge());
            }
        }
    }
}