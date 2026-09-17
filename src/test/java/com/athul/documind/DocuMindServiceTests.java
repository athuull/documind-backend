package com.athul.documind;

import com.athul.documind.DTO.*;
import com.athul.documind.Entity.Client;
import com.athul.documind.Entity.Role;
import com.athul.documind.Entity.User;
import com.athul.documind.Enum.PolicyStatus;
import com.athul.documind.Enum.PolicyType;
import com.athul.documind.Enum.RoleType;
import com.athul.documind.Repository.ClientRepository;
import com.athul.documind.Repository.PolicyRepository;
import com.athul.documind.Repository.RoleRepository;
import com.athul.documind.Repository.UserRepository;
import com.athul.documind.Service.ClientService;
import com.athul.documind.Service.PolicyService;
import com.athul.documind.Service.VehiclePolicyService;
import com.athul.documind.Service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DocuMindServiceTests {

    @Autowired
    private ClientService clientService;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehiclePolicyService vehiclePolicyService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PolicyRepository policyRepository;

    private User testUser;
    private Client testClient;

    @BeforeEach
    void setUp() {
        Role userRole = roleRepository.findByName(RoleType.USER)
                .orElseGet(() -> roleRepository.save(new Role(null, RoleType.USER, "Default user", 0L)));

        testUser = userRepository.findByEmail("test_agent@documind.ai")
                .orElseGet(() -> {
                    User u = new User();
                    u.setName("Test Agent");
                    u.setEmail("test_agent@documind.ai");
                    u.setPasswordHash("encoded_hash");
                    u.setRole(userRole);
                    u.setIsActive(true);
                    return userRepository.save(u);
                });

        ClientCreateRequestDTO clientDTO = new ClientCreateRequestDTO();
        clientDTO.setName("Acme Corporation");
        clientDTO.setEmail("contact@acme.com");
        clientDTO.setPhone("+1-555-0199");
        clientDTO.setAddress("123 Enterprise Blvd");

        ClientResponseDTO savedClient = clientService.createClient(clientDTO, testUser.getId());
        testClient = clientRepository.findById(savedClient.getId()).orElseThrow();
    }

    @Test
    @DisplayName("Should create and retrieve client and client dashboard")
    void testClientLifecycleAndDashboard() {
        // Create vehicle for client
        VehicleCreateRequestDTO vehicleDTO = new VehicleCreateRequestDTO();
        vehicleDTO.setRegistrationNumber("KA01AB" + System.currentTimeMillis() % 10000);
        vehicleDTO.setMake("Tesla");
        vehicleDTO.setModel("Model 3");
        vehicleDTO.setYear(2024);
        vehicleDTO.setClientId(testClient.getId());

        VehicleResponseDTO vehicleResp = vehicleService.createVehicle(vehicleDTO);
        assertNotNull(vehicleResp.getId());
        assertEquals(2024, vehicleResp.getYear());

        // Create health policy
        PolicyRequestDTO policyDTO = PolicyRequestDTO.builder()
                .policyNumber("POL-H-" + System.currentTimeMillis())
                .clientId(testClient.getId())
                .insuredName("Acme Key Person")
                .provider("Star Health")
                .premium(BigDecimal.valueOf(15000))
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusMonths(11))
                .policyType(PolicyType.HEALTH)
                .coverageType("Comprehensive")
                .coverageAmount(1000000.0)
                .insuredPersonAge(35)
                .build();

        PolicyResponseDTO policyResp = policyService.create(policyDTO);
        assertNotNull(policyResp.getId());
        assertEquals(PolicyStatus.ACTIVE, policyResp.getStatus());

        // Get Client 360 Dashboard
        ClientDashboardDTO dashboard = clientService.getClientDashboard(testClient.getId());
        assertNotNull(dashboard);
        assertEquals("Acme Corporation", dashboard.getClientName());
        assertEquals(1, dashboard.getTotalVehicles());
        assertEquals(1, dashboard.getTotalPolicies());
        assertEquals(1, dashboard.getActivePoliciesCount());
        assertTrue(dashboard.getTotalAnnualPremium().compareTo(BigDecimal.ZERO) > 0);

        // Search clients
        List<ClientResponseDTO> searchResults = clientService.searchClients("Acme");
        assertFalse(searchResults.isEmpty());
    }

    @Test
    @DisplayName("Should create vehicle policy directly via VehiclePolicyService without constraint violations")
    void testVehiclePolicyCreationDirectly() {
        VehicleCreateRequestDTO vehicleDTO = new VehicleCreateRequestDTO();
        vehicleDTO.setRegistrationNumber("MH02CD" + System.currentTimeMillis() % 10000);
        vehicleDTO.setMake("BMW");
        vehicleDTO.setModel("3 Series");
        vehicleDTO.setYear(2023);
        vehicleDTO.setClientId(testClient.getId());

        VehicleResponseDTO vehicleResp = vehicleService.createVehicle(vehicleDTO);

        VehiclePolicyRequest req = new VehiclePolicyRequest(
                "VP-" + System.currentTimeMillis(),
                "John Driver",
                "HDFC ERGO",
                BigDecimal.valueOf(25000),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                vehicleResp.getId()
        );

        VehiclePolicyResponse vPolicyResp = vehiclePolicyService.create(req);
        assertNotNull(vPolicyResp.policyId());
        assertEquals("BMW", vehicleResp.getMake());
        assertEquals(vehicleResp.getId(), vPolicyResp.vehicleId());
    }

    @Test
    @DisplayName("Should validate policy dates and reject end dates before start dates")
    void testPolicyDateValidation() {
        PolicyRequestDTO invalidDatePolicy = PolicyRequestDTO.builder()
                .policyNumber("INV-" + System.currentTimeMillis())
                .clientId(testClient.getId())
                .insuredName("Invalid Dates")
                .provider("ICICI Lombard")
                .premium(BigDecimal.valueOf(5000))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().minusDays(5)) // invalid!
                .policyType(PolicyType.VEHICLE)
                .build();

        assertThrows(IllegalArgumentException.class, () -> policyService.create(invalidDatePolicy));
    }

    @Test
    @DisplayName("Should update policy status and get agency stats")
    void testPolicyStatusTransitionsAndStats() {
        PolicyRequestDTO policyDTO = PolicyRequestDTO.builder()
                .policyNumber("STAT-" + System.currentTimeMillis())
                .clientId(testClient.getId())
                .insuredName("Acme Executive")
                .provider("Tata AIG")
                .premium(BigDecimal.valueOf(12000))
                .startDate(LocalDate.now().minusMonths(2))
                .endDate(LocalDate.now().plusDays(15)) // expiring soon
                .policyType(PolicyType.HEALTH)
                .insuredPersonAge(40)
                .build();

        PolicyResponseDTO created = policyService.create(policyDTO);
        assertEquals(PolicyStatus.ACTIVE, created.getStatus());

        // Check expiring policies
        List<PolicyResponseDTO> expiring = policyService.getExpiringPolicies(30);
        assertTrue(expiring.stream().anyMatch(p -> p.getId().equals(created.getId())));

        // Transition status to CANCELLED
        PolicyResponseDTO cancelled = policyService.updateStatus(created.getId(), PolicyStatus.CANCELLED);
        assertEquals(PolicyStatus.CANCELLED, cancelled.getStatus());

        // Check Agency Stats
        Map<String, Object> stats = policyService.getAgencyStats();
        assertNotNull(stats.get("totalPolicies"));
        assertNotNull(stats.get("cancelledPolicies"));
    }
}
