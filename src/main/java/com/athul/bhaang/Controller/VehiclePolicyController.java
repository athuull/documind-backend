package com.athul.bhaang.Controller;

import com.athul.bhaang.DTO.VehiclePolicyRequest;
import com.athul.bhaang.DTO.VehiclePolicyResponse;
import com.athul.bhaang.Service.VehiclePolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicle-policies")
@RequiredArgsConstructor
public class VehiclePolicyController {

    private final VehiclePolicyService service;

    // CREATE
    @PostMapping
    public ResponseEntity<VehiclePolicyResponse> create(
            @Valid @RequestBody VehiclePolicyRequest request
    ) {
        VehiclePolicyResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<VehiclePolicyResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<VehiclePolicyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<VehiclePolicyResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody VehiclePolicyRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}