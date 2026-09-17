package com.athul.documind.Controller;

import com.athul.documind.DTO.VehicleCreateRequestDTO;
import com.athul.documind.DTO.VehicleResponseDTO;
import com.athul.documind.DTO.VehicleUpdateRequestDTO;
import com.athul.documind.Service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponseDTO> create(@Valid @RequestBody VehicleCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.createVehicle(request));
    }

    @PutMapping
    public ResponseEntity<VehicleResponseDTO> update(@Valid @RequestBody VehicleUpdateRequestDTO request) {
        return ResponseEntity.ok(vehicleService.updateVehicle(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> updateWithId(
            @PathVariable Long id,
            @Valid @RequestBody VehicleUpdateRequestDTO request) {
        request.setId(id);
        return ResponseEntity.ok(vehicleService.updateVehicle(request));
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponseDTO>> getAll() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @GetMapping("/registration/{regNo}")
    public ResponseEntity<VehicleResponseDTO> getByRegistration(@PathVariable String regNo) {
        return ResponseEntity.ok(vehicleService.getVehicleByRegistration(regNo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}