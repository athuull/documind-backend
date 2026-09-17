package com.athul.documind.Controller;

import com.athul.documind.DTO.PolicyRequestDTO;
import com.athul.documind.DTO.PolicyResponseDTO;
import com.athul.documind.Enum.PolicyStatus;
import com.athul.documind.Enum.PolicyType;
import com.athul.documind.Service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    // Create a new policy
    @PostMapping
    public ResponseEntity<PolicyResponseDTO> createPolicy(@Valid @RequestBody PolicyRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.create(dto));
    }

    // Update an existing policy
    @PutMapping("/{id}")
    public ResponseEntity<PolicyResponseDTO> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody PolicyRequestDTO dto) {
        return ResponseEntity.ok(policyService.update(id, dto));
    }

    // Update policy status (e.g. MANUAL_REVIEW -> ACTIVE, or CANCELLED)
    @PatchMapping("/{id}/status")
    public ResponseEntity<PolicyResponseDTO> updatePolicyStatus(
            @PathVariable Long id,
            @RequestParam PolicyStatus status) {
        return ResponseEntity.ok(policyService.updateStatus(id, status));
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

    // Get all policies
    @GetMapping
    public ResponseEntity<List<PolicyResponseDTO>> getAllPolicies() {
        return ResponseEntity.ok(policyService.getAll());
    }

    // Get policies by client ID
    @GetMapping("/by-client/{clientId}")
    public ResponseEntity<List<PolicyResponseDTO>> getPoliciesByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(policyService.getByClientId(clientId));
    }

    // Get policy by policy number
    @GetMapping("/by-number/{policyNumber}")
    public ResponseEntity<PolicyResponseDTO> getByPolicyNumber(@PathVariable String policyNumber) {
        return ResponseEntity.ok(policyService.getByPolicyNumber(policyNumber));
    }

    // Get policies by type (HEALTH or VEHICLE)
    @GetMapping("/by-type/{type}")
    public ResponseEntity<List<PolicyResponseDTO>> getPoliciesByType(@PathVariable PolicyType type) {
        return ResponseEntity.ok(policyService.getByType(type));
    }

    // Get policies by status (ACTIVE, MANUAL_REVIEW, EXPIRED, CANCELLED)
    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<PolicyResponseDTO>> getPoliciesByStatus(@PathVariable PolicyStatus status) {
        return ResponseEntity.ok(policyService.getByStatus(status));
    }

    // Get policies expiring soon (renewal pipeline)
    @GetMapping("/expiring")
    public ResponseEntity<List<PolicyResponseDTO>> getExpiringPolicies(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(policyService.getExpiringPolicies(days));
    }

    // Search policies by query
    @GetMapping("/search")
    public ResponseEntity<List<PolicyResponseDTO>> searchPolicies(@RequestParam String query) {
        return ResponseEntity.ok(policyService.searchPolicies(query));
    }

    // Agency portfolio statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAgencyStats() {
        return ResponseEntity.ok(policyService.getAgencyStats());
    }
}