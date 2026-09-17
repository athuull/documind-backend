package com.athul.documind.Service;

import com.athul.documind.DTO.UserCreateRequestDTO;
import com.athul.documind.DTO.UserResponseDTO;
import com.athul.documind.DTO.UserUpdateRequestDTO;
import com.athul.documind.Entity.Role;
import com.athul.documind.Entity.User;
import com.athul.documind.Exception.EmailAlreadyExistsException;
import com.athul.documind.Exception.RoleNotFoundException;
import com.athul.documind.Exception.UserNotFoundException;
import com.athul.documind.Repository.RoleRepository;
import com.athul.documind.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // =========================
    // FIND BY EMAIL
    // =========================
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    // =========================
    // GET CURRENT AUTHENTICATED USER
    // =========================
    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        String email = authentication.getName();
        return findByEmail(email);
    }

    // =========================
    // CREATE USER
    // =========================
    @Transactional
    public UserResponseDTO createUser(UserCreateRequestDTO request) {
        userRepository.findByEmail(request.getEmail())
                .ifPresent(existing -> {
                    throw new EmailAlreadyExistsException("Email already exists");
                });

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    // ... rest of your existing methods ...

    // =========================
    // UPDATE USER (Optimistic Locking)
    // =========================
    @Transactional
    public UserResponseDTO updateUser(UserUpdateRequestDTO request) {
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        userRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new EmailAlreadyExistsException("Email already exists");
                });

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

        if (!user.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockingFailureException(
                    "The record was updated by another user. Please refresh and try again."
            );
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(role);
        user.setIsActive(request.getIsActive());

        User updated = userRepository.saveAndFlush(user);

        return mapToResponse(updated);
    }

    // =========================
    // GET ALL USERS
    // =========================
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================
    // GET USER BY ID
    // =========================
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return mapToResponse(user);
    }

    // =========================
    // DELETE USER
    // =========================
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    // =========================
    // MAPPER
    // =========================
    private UserResponseDTO mapToResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roleName(user.getRole() != null ? String.valueOf(user.getRole().getName()) : null)
                .isActive(user.getIsActive())
                .version(user.getVersion())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}