package com.athul.documind.Service;

import com.athul.documind.DTO.VehicleCreateRequestDTO;
import com.athul.documind.DTO.VehicleResponseDTO;
import com.athul.documind.DTO.VehicleUpdateRequestDTO;
import com.athul.documind.Entity.Client;
import com.athul.documind.Entity.Vehicle;
import com.athul.documind.Exception.ClientNotFoundException;
import com.athul.documind.Exception.VehicleNotFoundException;
import com.athul.documind.Mapper.VehicleMapper;
import com.athul.documind.Repository.ClientRepository;
import com.athul.documind.Repository.VehicleRepository;
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
    public VehicleResponseDTO getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with id: " + id));
        return mapToResponse(vehicle);
    }

    @Transactional(readOnly = true)
    public VehicleResponseDTO getVehicleByRegistration(String regNo) {
        Vehicle vehicle = vehicleRepository.findByRegistrationNumber(regNo)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with registration: " + regNo));
        return mapToResponse(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new VehicleNotFoundException("Vehicle not found with id: " + id);
        }
        vehicleRepository.deleteById(id);
    }

    // =========================
    // MAPPER
    // =========================
    private VehicleResponseDTO mapToResponse(Vehicle vehicle) {
        return vehicleMapper.toResponse(vehicle);
    }
}