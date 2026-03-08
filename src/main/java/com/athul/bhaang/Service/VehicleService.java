package com.athul.bhaang.Service;

import com.athul.bhaang.DTO.VehicleCreateRequestDTO;
import com.athul.bhaang.DTO.VehicleResponseDTO;
import com.athul.bhaang.DTO.VehicleUpdateRequestDTO;
import com.athul.bhaang.Entity.Client;
import com.athul.bhaang.Entity.Vehicle;
import com.athul.bhaang.Exception.ClientNotFoundException;
import com.athul.bhaang.Exception.VehicleNotFoundException;
import com.athul.bhaang.Mapper.VehicleMapper;
import com.athul.bhaang.Repository.ClientRepository;
import com.athul.bhaang.Repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ClientRepository clientRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional
    public VehicleResponseDTO createVehicle(VehicleCreateRequestDTO request) {
        if (vehicleRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new IllegalArgumentException("Registration number already exists.");
        }

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found"));

        Vehicle vehicle = vehicleMapper.toEntity(request);
        vehicle.setClient(client);


        Vehicle saved = vehicleRepository.save(vehicle);
        return mapToResponse(saved);
    }

    @Transactional
    public VehicleResponseDTO updateVehicle(VehicleUpdateRequestDTO request) {
        Vehicle vehicle = vehicleRepository.findById(request.getId())
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));

        // 1. Version Check
        if (!vehicle.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockingFailureException("Vehicle was updated by another user.");
        }

        // 2. Client Update (Null-safe check)
        if (vehicle.getClient() == null || !vehicle.getClient().getId().equals(request.getClientId())) {
            Client newClient = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ClientNotFoundException("New client not found"));
            vehicle.setClient(newClient);
        }

        // 3. Map basic fields (Make sure Entity has 'make', not 'brand')
        vehicleMapper.updateVehicleFromDto(request, vehicle);

        // 4. Save and Flush to trigger version increment
        Vehicle updated = vehicleRepository.saveAndFlush(vehicle);

        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getAllVehicles() {
        return vehicleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponseDTO getVehicleByRegistration(String regNo) {
        Vehicle vehicle = vehicleRepository.findByRegistrationNumber(regNo)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));
        return mapToResponse(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new VehicleNotFoundException("Vehicle not found");
        }
        vehicleRepository.deleteById(id);
    }

    // =========================
    // MAPPER (Handles the missing client name issue)
    // =========================
    private VehicleResponseDTO mapToResponse(Vehicle vehicle) {
        return VehicleResponseDTO.builder()
                .id(vehicle.getId())
                .registrationNumber(vehicle.getRegistrationNumber())
                .make(vehicle.getMake()) // Ensure this is .getMake() in Entity
                .model(vehicle.getModel())
                .clientName(vehicle.getClient() != null ? vehicle.getClient().getName() : "No Client Assigned")
                .version(vehicle.getVersion())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}