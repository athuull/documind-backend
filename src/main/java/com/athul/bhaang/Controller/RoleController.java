package com.athul.bhaang.Controller;

import com.athul.bhaang.DTO.RoleCreateRequestDTO;
import com.athul.bhaang.DTO.RoleResponseDTO;
import com.athul.bhaang.DTO.RoleUpdateRequestDTO;
import com.athul.bhaang.Enum.RoleType;
import com.athul.bhaang.Service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    // CREATE ROLE
    @PostMapping
    public RoleResponseDTO createRole(@Valid @RequestBody RoleCreateRequestDTO request) {
        return roleService.createRole(request);
    }

    // GET ALL ROLES
    @GetMapping
    public List<RoleResponseDTO> getAllRoles() {
        return roleService.getAllRoles();
    }

    // GET ROLE BY ID
    @GetMapping("/{id}")
    public RoleResponseDTO getRole(@PathVariable Long id) {
        return roleService.getRoleById(id);
    }

    // UPDATE ROLE (Optimistic Locking)
    @PutMapping("/{id}")
    @Retryable(
            value = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public RoleResponseDTO updateRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequestDTO request) {

        request.setId(id);
        return roleService.updateRole(request);
    }

    // DELETE ROLE
    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
    }
}