package com.athul.bhaang.Service;

import com.athul.bhaang.DTO.RoleCreateRequestDTO;
import com.athul.bhaang.DTO.RoleResponseDTO;
import com.athul.bhaang.DTO.RoleUpdateRequestDTO;
import com.athul.bhaang.Entity.Role;
import com.athul.bhaang.Enum.RoleType;
import com.athul.bhaang.Repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    // Ensure only roleRepository is used here
    private final RoleRepository roleRepository;

    // =========================
    // CREATE ROLE
    // =========================
    @Transactional
    public RoleResponseDTO createRole(RoleCreateRequestDTO request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Role already exists");
        }

        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());

        // FIXED: Changed from userRepository to roleRepository
        Role saved = roleRepository.save(role);
        return mapToResponse(saved);
    }

    // =========================
    // UPDATE ROLE
    // =========================
    @Transactional
    public RoleResponseDTO updateRole(RoleUpdateRequestDTO request) {
        Role role = roleRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        // Optimistic Locking Check
        if (!role.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockingFailureException("Role was updated by another admin.");
        }

        role.setName(request.getName());
        role.setDescription(request.getDescription());

        // Save and Flush to sync the version increment immediately
        Role updated = roleRepository.saveAndFlush(role);

        return mapToResponse(updated);
    }

    // =========================
    // READ OPERATIONS
    // =========================
    @Transactional(readOnly = true)
    public List<RoleResponseDTO> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponseDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        return mapToResponse(role);
    }

    @Transactional(readOnly = true)
    public RoleResponseDTO getRoleByName(RoleType name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        return mapToResponse(role);
    }

    // =========================
    // DELETE ROLE
    // =========================
    @Transactional
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new EntityNotFoundException("Role not found");
        }
        roleRepository.deleteById(id);
    }

    // =========================
    // MAPPER
    // =========================
    private RoleResponseDTO mapToResponse(Role role) {
        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                // description should probably be in the response too!
                .version(role.getVersion())
                .build();
    }
}