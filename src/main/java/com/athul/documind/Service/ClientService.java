package com.athul.documind.Service;

import com.athul.documind.DTO.ClientCreateRequestDTO;
import com.athul.documind.DTO.ClientDashboardDTO;
import com.athul.documind.DTO.ClientResponseDTO;
import com.athul.documind.DTO.ClientUpdateRequestDTO;
import com.athul.documind.Entity.Client;
import com.athul.documind.Entity.Policy;
import com.athul.documind.Entity.User;
import com.athul.documind.Entity.Vehicle;
import com.athul.documind.Entity.VehiclePolicy;
import com.athul.documind.Enum.PolicyStatus;
import com.athul.documind.Exception.ClientNotFoundException;
import com.athul.documind.Exception.UserNotFoundException;
import com.athul.documind.Repository.ClientRepository;
import com.athul.documind.Repository.DocumentRepository;
import com.athul.documind.Repository.PolicyRepository;
import com.athul.documind.Repository.UserRepository;
import com.athul.documind.Repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PolicyRepository policyRepository;
    private final VehicleRepository vehicleRepository;
    private final DocumentRepository documentRepository;

    // =========================
    // CREATE CLIENT
    // =========================
    @Transactional
    public ClientResponseDTO createClient(ClientCreateRequestDTO request, Long createdByUserId) {
        User user = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + createdByUserId));

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
        Client client = clientRepository.findById(request.getId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + request.getId()));

        // Optimistic locking check
        if (request.getVersion() != null && !client.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockingFailureException(
                    "Client record was updated by another user. Please refresh."
            );
        }

        client.setName(request.getName());
        client.setPhone(request.getPhone());
        client.setEmail(request.getEmail());
        client.setAddress(request.getAddress());

        if (request.getAssignedToUserId() != null &&
                (client.getAssignedTo() == null || !client.getAssignedTo().getId().equals(request.getAssignedToUserId()))) {
            User newAssignee = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new UserNotFoundException("New assignee not found with id: " + request.getAssignedToUserId()));
            client.setAssignedTo(newAssignee);
        }

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
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + id));
        return mapToResponse(client);
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getClientsByUserId(Long userId) {
        return clientRepository.findByCreatedById(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getClientsAssignedToUser(Long userId) {
        return clientRepository.findByAssignedToId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> searchClients(String query) {
        if (query == null || query.isBlank()) {
            return getAllClients();
        }
        return clientRepository.searchClients(query.trim())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================
    // CLIENT 360 DASHBOARD
    // =========================
    @Transactional(readOnly = true)
    public ClientDashboardDTO getClientDashboard(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + clientId));

        List<Vehicle> vehicles = vehicleRepository.findByClient(client);
        List<Policy> policies = policyRepository.findByClientId(clientId);

        LocalDate today = LocalDate.now();
        LocalDate expiryWindow = today.plusDays(30);

        long activePoliciesCount = policies.stream()
                .filter(p -> p.getStatus() == PolicyStatus.ACTIVE)
                .count();

        BigDecimal totalPremium = policies.stream()
                .filter(p -> p.getStatus() == PolicyStatus.ACTIVE && p.getPremium() != null)
                .map(Policy::getPremium)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Map vehicles with active policies
        List<ClientDashboardDTO.VehicleSummary> vehicleSummaries = vehicles.stream()
                .map(v -> {
                    // Find active policy for this vehicle
                    Policy activePol = policies.stream()
                            .filter(p -> p instanceof VehiclePolicy vp && vp.getVehicle() != null && vp.getVehicle().getId().equals(v.getId()))
                            .findFirst()
                            .orElse(null);

                    return ClientDashboardDTO.VehicleSummary.builder()
                            .vehicleId(v.getId())
                            .make(v.getMake())
                            .model(v.getModel())
                            .year(v.getYear())
                            .regNumber(v.getRegistrationNumber())
                            .activePolicyNumber(activePol != null ? activePol.getPolicyNumber() : "None")
                            .currentStatus(activePol != null ? activePol.getStatus() : null)
                            .build();
                })
                .toList();

        // Map policies
        List<ClientDashboardDTO.PolicySummary> policySummaries = policies.stream()
                .map(p -> ClientDashboardDTO.PolicySummary.builder()
                        .policyId(p.getPolicyId())
                        .policyNumber(p.getPolicyNumber())
                        .policyType(p.getType())
                        .status(p.getStatus())
                        .provider(p.getProvider())
                        .premium(p.getPremium())
                        .startDate(p.getStartDate())
                        .endDate(p.getEndDate())
                        .isExpiringSoon(p.getStatus() == PolicyStatus.ACTIVE && p.getEndDate() != null &&
                                !p.getEndDate().isBefore(today) && !p.getEndDate().isAfter(expiryWindow))
                        .build())
                .toList();

        return ClientDashboardDTO.builder()
                .clientId(client.getId())
                .clientName(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .assignedToAgent(client.getAssignedTo() != null ? client.getAssignedTo().getName() : "Unassigned")
                .totalVehicles(vehicles.size())
                .totalPolicies(policies.size())
                .activePoliciesCount(activePoliciesCount)
                .totalAnnualPremium(totalPremium)
                .totalDocuments(policies.size()) // estimated documents on file
                .vehicles(vehicleSummaries)
                .policies(policySummaries)
                .build();
    }

    // =========================
    // DELETE CLIENT
    // =========================
    @Transactional
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ClientNotFoundException("Client not found with id: " + id);
        }
        clientRepository.deleteById(id);
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
                .createdByUserName(client.getCreatedBy() != null ? client.getCreatedBy().getName() : "System")
                .assignedToUserName(client.getAssignedTo() != null ? client.getAssignedTo().getName() : "Unassigned")
                .version(client.getVersion())
                .build();
    }
}