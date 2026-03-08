package com.athul.bhaang.Service;

import com.athul.bhaang.DTO.VehiclePolicyRequest;
import com.athul.bhaang.DTO.VehiclePolicyResponse;
import com.athul.bhaang.Entity.Vehicle;
import com.athul.bhaang.Entity.VehiclePolicy;
import com.athul.bhaang.Repository.VehiclePolicyRepository;
import com.athul.bhaang.Repository.VehicleRepository;
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
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));

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
                .orElseThrow(() -> new EntityNotFoundException("Vehicle policy not found"));
        return mapToResponse(policy);
    }

    public VehiclePolicyResponse update(Long id, VehiclePolicyRequest request) {

        VehiclePolicy policy = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle policy not found"));

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));

        mapRequestToEntity(policy, request, vehicle);

        return mapToResponse(repository.save(policy));
    }

    public void delete(Long id) {
        if (!repository.existsById(id))
            throw new EntityNotFoundException("Vehicle policy not found");

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
                policy.getVehicle().getId(),
                policy.getVehicle().getRegistrationNumber()

        );

    }
}