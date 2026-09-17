package com.athul.documind.Controller;

import com.athul.documind.DTO.ClientCreateRequestDTO;
import com.athul.documind.DTO.ClientDashboardDTO;
import com.athul.documind.DTO.ClientResponseDTO;
import com.athul.documind.DTO.ClientUpdateRequestDTO;
import com.athul.documind.Entity.User;
import com.athul.documind.Security.CustomUserDetails;
import com.athul.documind.Service.ClientService;
import com.athul.documind.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final UserService userService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<ClientResponseDTO> createClient(
            @Valid @RequestBody ClientCreateRequestDTO request,
            @PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.createClient(request, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientUpdateRequestDTO request) {
        request.setId(id);
        return ResponseEntity.ok(clientService.updateClient(request));
    }

    @GetMapping("/my-clients")
    public ResponseEntity<List<ClientResponseDTO>> getMyClients(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(clientService.getClientsByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @GetMapping("/{id}/dashboard")
    public ResponseEntity<ClientDashboardDTO> getClientDashboard(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientDashboard(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClientResponseDTO>> searchClients(@RequestParam String query) {
        return ResponseEntity.ok(clientService.searchClients(query));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getId();
        } else if (principal instanceof User u) {
            return u.getId();
        } else {
            return userService.findByEmail(authentication.getName()).getId();
        }
    }
}
