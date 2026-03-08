package com.athul.bhaang.Service;

import com.athul.bhaang.DTO.ClientCreateRequestDTO;
import com.athul.bhaang.DTO.ClientResponseDTO;
import com.athul.bhaang.DTO.ClientUpdateRequestDTO;
import com.athul.bhaang.Entity.Client;
import com.athul.bhaang.Entity.User;
import com.athul.bhaang.Exception.ClientNotFoundException;
import com.athul.bhaang.Exception.UserNotFoundException;
import com.athul.bhaang.Repository.ClientRepository;
import com.athul.bhaang.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    // =========================
    // CREATE CLIENT
    // =========================
    @Transactional
    public ClientResponseDTO createClient(ClientCreateRequestDTO request, Long createdByUserId) {
        // Find the user creating this client
        User user = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Client client = new Client();
        client.setName(request.getName());
        client.setPhone(request.getPhone());
        client.setEmail(request.getEmail());
        client.setAddress(request.getAddress());

        // Logic: Person who creates it is also the initial assignee
        client.setCreatedBy(user);
        client.setAssignedTo(user);

        Client saved = clientRepository.save(client);
        return mapToResponse(saved);
    }

    // =========================
    // UPDATE CLIENT (Optimistic Locking)
    // =========================
    @Transactional
    public ClientResponseDTO updateClient(ClientUpdateRequestDTO request) {
        // 1. Fetch managed entity
        Client client = clientRepository.findById(request.getId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found"));

        // 2. MANUAL VERSION CHECK
        if (!client.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockingFailureException(
                    "Client record was updated by another user. Please refresh."
            );
        }

        // 3. Update basic fields
        client.setName(request.getName());
        client.setPhone(request.getPhone());
        client.setEmail(request.getEmail());
        client.setAddress(request.getAddress());

        // 4. Handle Reassignment if a new User ID is provided
        if (request.getAssignedToUserId() != null &&
                (client.getAssignedTo() == null || !client.getAssignedTo().getId().equals(request.getAssignedToUserId()))) {
            User newAssignee = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new UserNotFoundException("New assignee not found"));
            client.setAssignedTo(newAssignee);
        }

        // 5. SAVE AND FLUSH to increment version immediately
        Client updated = clientRepository.saveAndFlush(client);
        return mapToResponse(updated);
    }

    // =========================
    // READ OPERATIONS
    // =========================
    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getAllClients() {
        return clientRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientResponseDTO getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Client not found"));
        return mapToResponse(client);
    }

    // =========================
    // DELETE CLIENT
    // =========================
    @Transactional
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ClientNotFoundException("Client not found");
        }
        clientRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getClientsByUserId(Long userId) {

        return clientRepository.findByCreatedById(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================
    // INTERNAL MAPPER
    // =========================
    private ClientResponseDTO mapToResponse(Client client) {
        return ClientResponseDTO.builder()
                .id(client.getId())
                .name(client.getName())
                .phone(client.getPhone())
                .email(client.getEmail())
                .address(client.getAddress())
                // Safe checks for nested User objects
                .createdByUserName(client.getCreatedBy() != null ? client.getCreatedBy().getName() : "System")
                .assignedToUserName(client.getAssignedTo() != null ? client.getAssignedTo().getName() : "Unassigned")
                .version(client.getVersion())
                .build();
    }
}