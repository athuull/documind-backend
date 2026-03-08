package com.athul.bhaang.Controller;

import com.athul.bhaang.DTO.ClientCreateRequestDTO;
import com.athul.bhaang.DTO.ClientResponseDTO;
import com.athul.bhaang.Security.CustomUserDetails;
import com.athul.bhaang.Service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping("/user/{userId}")
    public ClientResponseDTO createClient(@RequestBody ClientCreateRequestDTO request, // FIX: Use DTO here
                                          @PathVariable Long userId) {
        return clientService.createClient(request, userId);
    }

    @GetMapping("/my-clients")
    public List<ClientResponseDTO> getMyClients(Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return clientService.getClientsByUserId(userId);
    }

    @GetMapping
    public List<ClientResponseDTO> getAllClients() { // FIX: Return DTO List
        return clientService.getAllClients();
    }

    @GetMapping("/{id}")
    public ClientResponseDTO getClient(@PathVariable Long id) { // FIX: Return DTO
        return clientService.getClientById(id);
    }

    @DeleteMapping("/{id}")

    public void deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
    }
}

