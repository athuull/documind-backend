package com.athul.bhaang.Service;

import com.athul.bhaang.DTO.HealthPolicyRequest;
import com.athul.bhaang.DTO.HealthPolicyResponse;
import com.athul.bhaang.Entity.Client;
import com.athul.bhaang.Entity.HealthPolicy;
import com.athul.bhaang.Enum.PolicyStatus;
import com.athul.bhaang.Enum.PolicyType;
import com.athul.bhaang.Exception.ClientNotFoundException;
import com.athul.bhaang.Repository.ClientRepository;
import com.athul.bhaang.Repository.HealthPolicyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HealthPolicyService {

    private final HealthPolicyRepository repository;
    private final ClientRepository clientRepository;

    public HealthPolicyResponse create(HealthPolicyRequest request) {
        // Fetch client
        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + request.clientId()));

        HealthPolicy policy = new HealthPolicy();
        policy.setClient(client);
        policy.setType(PolicyType.HEALTH); // Set the policy type
        policy.setStatus(PolicyStatus.ACTIVE); // Set default status

        mapRequestToEntity(policy, request);

        return mapToResponse(repository.save(policy));
    }

    @Transactional(readOnly = true)
    public List<HealthPolicyResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HealthPolicyResponse getById(Long id) {
        HealthPolicy policy = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Health policy not found with id: " + id));
        return mapToResponse(policy);
    }

    public HealthPolicyResponse update(Long id, HealthPolicyRequest request) {
        HealthPolicy policy = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Health policy not found with id: " + id));

        // Update client if changed
        if (!policy.getClient().getId().equals(request.clientId())) {
            Client newClient = clientRepository.findById(request.clientId())
                    .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + request.clientId()));
            policy.setClient(newClient);
        }

        mapRequestToEntity(policy, request);

        return mapToResponse(repository.save(policy));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Health policy not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private void mapRequestToEntity(HealthPolicy policy, HealthPolicyRequest request) {
        policy.setPolicyNumber(request.policyNumber());
        policy.setInsuredName(request.insuredName());
        policy.setProvider(request.provider());
        policy.setPremium(request.premium());
        policy.setStartDate(request.startDate());
        policy.setEndDate(request.endDate());

        // Health-specific fields
        policy.setInsuredPersonAge(request.insuredPersonAge());
        policy.setCoverageType(request.coverageType());
        policy.setCoverageAmount(request.coverageAmount());
        policy.setBeneficiaryName(request.beneficiaryName());

        // Set health policy type if provided
        if (request.healthPolicyType() != null) {
            policy.setHealthPolicyType(request.healthPolicyType());
        }
    }

    private HealthPolicyResponse mapToResponse(HealthPolicy policy) {
        return new HealthPolicyResponse(
                policy.getPolicyId(),
                policy.getPolicyNumber(),
                policy.getProvider(),
                policy.getInsuredName(),
                policy.getPremium(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getInsuredPersonAge(),
                policy.getHealthPolicyType()
        );
    }
}