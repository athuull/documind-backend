package com.athul.bhaang.Controller;

import com.athul.bhaang.DTO.HealthPolicyRequest;
import com.athul.bhaang.DTO.HealthPolicyResponse;
import com.athul.bhaang.Service.HealthPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/health-policies")
@RequiredArgsConstructor
public class HealthPolicyController {

    private final HealthPolicyService service;

    // CREATE
    @PostMapping
    public ResponseEntity<HealthPolicyResponse> create(
            @Valid @RequestBody HealthPolicyRequest request
    ) {
        HealthPolicyResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<HealthPolicyResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<HealthPolicyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<HealthPolicyResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody HealthPolicyRequest request
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