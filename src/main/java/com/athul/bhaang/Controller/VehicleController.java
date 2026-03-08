package com.athul.bhaang.Controller;

import com.athul.bhaang.DTO.VehicleCreateRequestDTO;
import com.athul.bhaang.DTO.VehicleResponseDTO;
import com.athul.bhaang.DTO.VehicleUpdateRequestDTO;
import com.athul.bhaang.Service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        return ResponseEntity.ok(vehicleService.createVehicle(request));
    }

    @PutMapping
    public ResponseEntity<VehicleResponseDTO> update(@Valid @RequestBody VehicleUpdateRequestDTO request) {
        return ResponseEntity.ok(vehicleService.updateVehicle(request));
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponseDTO>> getAll() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
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