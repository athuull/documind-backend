package com.athul.documind.Service;

import com.athul.documind.DTO.VehiclePolicyRequest;
import com.athul.documind.DTO.VehiclePolicyResponse;
import com.athul.documind.Entity.Vehicle;
import com.athul.documind.Entity.VehiclePolicy;
import com.athul.documind.Enum.PolicyStatus;
import com.athul.documind.Enum.PolicyType;
import com.athul.documind.Repository.VehiclePolicyRepository;
import com.athul.documind.Repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VehiclePolicyService {

    private final VehiclePolicyRepository repository;
    private final VehicleRepository vehicleRepository;

    public VehiclePolicyResponse create(VehiclePolicyRequest request) {

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + request.vehicleId()));

        VehiclePolicy policy = new VehiclePolicy();
        mapRequestToEntity(policy, request, vehicle);

        return mapToResponse(repository.save(policy));
    }

    @Transactional(readOnly = true)
    public List<VehiclePolicyResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehiclePolicyResponse getById(Long id) {
        VehiclePolicy policy = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle policy not found with id: " + id));
        return mapToResponse(policy);
    }

    public VehiclePolicyResponse update(Long id, VehiclePolicyRequest request) {

        VehiclePolicy policy = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle policy not found with id: " + id));

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + request.vehicleId()));

        mapRequestToEntity(policy, request, vehicle);

        return mapToResponse(repository.save(policy));
    }

    public void delete(Long id) {
        if (!repository.existsById(id))
            throw new EntityNotFoundException("Vehicle policy not found with id: " + id);

        repository.deleteById(id);
    }

    private void mapRequestToEntity(VehiclePolicy policy,
                                    VehiclePolicyRequest request,
                                    Vehicle vehicle) {

        policy.setPolicyNumber(request.policyNumber());
        policy.setInsuredName(request.insuredName());
        policy.setProvider(request.provider());
        policy.setPremium(request.premium());
        policy.setStartDate(request.startDate());
        policy.setEndDate(request.endDate());
        policy.setVehicle(vehicle);
        policy.setVehicleRegistration(vehicle.getRegistrationNumber());
        policy.setClient(vehicle.getClient());
        policy.setType(PolicyType.VEHICLE);
        if (policy.getStatus() == null) {
            policy.setStatus(PolicyStatus.ACTIVE);
        }
    }

    private VehiclePolicyResponse mapToResponse(VehiclePolicy policy) {
        return new VehiclePolicyResponse(
                policy.getPolicyId(),
                policy.getPolicyNumber(),
                policy.getProvider(),
                policy.getInsuredName(),
                policy.getPremium(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getVehicle() != null ? policy.getVehicle().getId() : null,
                policy.getVehicle() != null ? policy.getVehicle().getRegistrationNumber() : policy.getVehicleRegistration()
        );
    }
}