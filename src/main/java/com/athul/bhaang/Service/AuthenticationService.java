package com.athul.bhaang.Service;

import com.athul.bhaang.DTO.LoginUserDTO;
import com.athul.bhaang.DTO.RegisterUserDTO;
import com.athul.bhaang.Entity.Role;
import com.athul.bhaang.Entity.User;
import com.athul.bhaang.Enum.RoleType;
import com.athul.bhaang.Repository.RoleRepository;
import com.athul.bhaang.Repository.UserRepository;
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
            throw new RuntimeException("User with email " + input.getEmail() + " already exists");
        }

        // Find default USER role
        Role userRole = roleRepository.findByName(RoleType.USER)
                .orElseThrow(() -> new RuntimeException("Role USER not found. Please initialize roles."));

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
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}