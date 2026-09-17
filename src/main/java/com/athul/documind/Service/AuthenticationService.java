package com.athul.documind.Service;

import com.athul.documind.DTO.LoginUserDTO;
import com.athul.documind.DTO.RegisterUserDTO;
import com.athul.documind.DTO.UserResponseDTO;
import com.athul.documind.Entity.Role;
import com.athul.documind.Entity.User;
import com.athul.documind.Enum.RoleType;
import com.athul.documind.Exception.EmailAlreadyExistsException;
import com.athul.documind.Exception.RoleNotFoundException;
import com.athul.documind.Exception.UserNotFoundException;
import com.athul.documind.Repository.RoleRepository;
import com.athul.documind.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public User signup(RegisterUserDTO input) {
        // Check if user already exists
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new EmailAlreadyExistsException("User with email " + input.getEmail() + " already exists");
        }

        // Find default USER role
        Role userRole = roleRepository.findByName(RoleType.USER)
                .orElseThrow(() -> new RoleNotFoundException("Role USER not found. Please initialize roles."));

        // Create new user
        User user = new User();
        user.setName(input.getName());
        user.setEmail(input.getEmail());
        user.setPasswordHash(passwordEncoder.encode(input.getPassword()));
        user.setRole(userRole);
        user.setIsActive(true);

        return userRepository.save(user);
    }

    public User authenticate(LoginUserDTO input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        return userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + input.getEmail()));
    }

    public UserResponseDTO mapToResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roleName(user.getRole() != null ? user.getRole().getName().name() : null)
                .isActive(user.getIsActive())
                .version(user.getVersion())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}