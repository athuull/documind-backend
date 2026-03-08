package com.athul.bhaang.Controller;

import com.athul.bhaang.DTO.PolicyRequestDTO;
import com.athul.bhaang.DTO.PolicyResponseDTO;
import com.athul.bhaang.Enum.PolicyType;
import com.athul.bhaang.Service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    // Create a new policy (dispatches to VehiclePolicyService or HealthPolicyService)
    @PostMapping
    public ResponseEntity<PolicyResponseDTO> createPolicy(@Valid @RequestBody PolicyRequestDTO dto) {
        return ResponseEntity.ok(policyService.create(dto));
    }

    // Update an existing policy
    @PutMapping("/{id}")
    public ResponseEntity<PolicyResponseDTO> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody PolicyRequestDTO dto) {
        return ResponseEntity.ok(policyService.update(id, dto));
    }

    // Delete a policy
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        policyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Get policy by ID
    @GetMapping("/{id}")
    public ResponseEntity<PolicyResponseDTO> getPolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getById(id));
    }

    // Get all policies (both vehicle and health)
    @GetMapping
    public ResponseEntity<List<PolicyResponseDTO>> getAllPolicies() {
        return ResponseEntity.ok(policyService.getAll());
    }

    // Get policies by client ID
    @GetMapping("/by-client/{clientId}")
    public ResponseEntity<List<PolicyResponseDTO>> getPoliciesByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(Collections.singletonList(policyService.getById(clientId)));
    }


//    // Get policies by type (HEALTH or VEHICLE)
//    @GetMapping("/by-type/{type}")
//    public ResponseEntity<List<PolicyResponseDTO>> getPoliciesByType(@PathVariable PolicyType type) {
//        return ResponseEntity.ok(policyService.getPoliciesByType(type));
//    }
}