package com.athul.documind.Controller;

import com.athul.documind.DTO.UserCreateRequestDTO;
import com.athul.documind.DTO.UserResponseDTO;
import com.athul.documind.DTO.UserUpdateRequestDTO;
import com.athul.documind.Entity.User;
import com.athul.documind.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // =========================
    // GET CURRENT USER
    // =========================
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> authenticatedUser() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        return ResponseEntity.ok(userService.getUserById(currentUser.getId()));
    }

    // =========================
    // CREATE USER
    // =========================
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserCreateRequestDTO request) {
        UserResponseDTO response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================
    // GET ALL USERS
    // =========================
    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // =========================
    // GET USER BY ID
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // =========================
    // UPDATE USER
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDTO request) {
        request.setId(id);
        UserResponseDTO updatedUser = userService.updateUser(request);
        return ResponseEntity.ok(updatedUser);
    }

    // =========================
    // DELETE USER
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}