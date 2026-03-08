package com.athul.bhaang.Service;

import com.athul.bhaang.DTO.DocumentProcessingResponseDTO;
import com.athul.bhaang.DTO.PolicyExtractionDTO;
import com.athul.bhaang.Entity.*;
import com.athul.bhaang.Enum.PolicyStatus;
import com.athul.bhaang.Enum.PolicyType;
import com.athul.bhaang.Repository.ClientRepository;
import com.athul.bhaang.Repository.DocumentRepository;
import com.athul.bhaang.Repository.PolicyRepository;
import com.athul.bhaang.Repository.VehicleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessingService {

    private final PDFParserService pdfParserService;
    private final PolicyExtractionService extractionService;
    private final FileStorageService fileStorageService;
    private final DocumentRepository documentRepository;
    private final PolicyRepository policyRepository;
    private final ClientRepository clientRepository;
    private final VectorStore vectorStore;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public DocumentProcessingResponseDTO processPolicyUpload(MultipartFile file, Long clientId) throws IOException {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new RuntimeException("File must have a name");
        }

        String savedPath = fileStorageService.saveFile(file);

        // 🔥 AI extraction (STRUCTURED OUTPUT enforced inside extractionService)
        PolicyExtractionDTO aiDto = extractionService.extractPolicyData(file, client);

        Policy policy = createPolicyFromExtraction(aiDto, client);

        Policy savedPolicy = policyRepository.save(policy);

        saveDocumentEntity(savedPolicy, originalFilename, savedPath);

        if (aiDto.getRawText() != null && !aiDto.getRawText().isBlank()) {
            ingestMetadataToAI(savedPolicy, aiDto.getRawText(), clientId);
        }

        return buildResponse(savedPolicy, aiDto);    }

    private Policy createPolicyFromExtraction(PolicyExtractionDTO aiDto, Client client) {

        PolicyType policyType = aiDto.getPolicyType();

        if (policyType == null) {
            log.warn("AI returned null policy type. Defaulting to VEHICLE.");
            policyType = PolicyType.VEHICLE;
        }

        if (policyType != PolicyType.VEHICLE && policyType != PolicyType.HEALTH) {
            throw new IllegalArgumentException("AI returned invalid policy type.");
        }

        Policy policy;

        if (policyType == PolicyType.VEHICLE) {

            VehiclePolicy vehiclePolicy = new VehiclePolicy();
            vehiclePolicy.setVehicleRegistration(aiDto.getVehicleRegistration());
            vehiclePolicy.setVehicleType(aiDto.getVehicleType());
            vehiclePolicy.setCoverageType(aiDto.getCoverageType());

            String registration = aiDto.getVehicleRegistration();

            if (registration == null || registration.isBlank()) {

                log.warn("AI classified as VEHICLE but registration missing. Marking for manual review.");

                vehiclePolicy.setCoverageType(aiDto.getCoverageType());
                vehiclePolicy.setVehicleType(aiDto.getVehicleType());

                vehiclePolicy.setClient(client);
                vehiclePolicy.setPolicyNumber(aiDto.getPolicyNumber());
                vehiclePolicy.setType(PolicyType.VEHICLE);
                vehiclePolicy.setStatus(PolicyStatus.MANUAL_REVIEW);

                return vehiclePolicy;
            }

            // 🔥 GUARANTEE vehicle_id is never null
            Vehicle vehicle = vehicleRepository
                    .findByRegistrationNumber(registration.trim().toUpperCase())
                    .orElseGet(() -> {
                        log.info("Vehicle not found. Creating new vehicle for registration: {}", registration);

                        Vehicle newVehicle = new Vehicle();
                        newVehicle.setRegistrationNumber(registration.trim().toUpperCase());
                        newVehicle.setClient(client);

                        return vehicleRepository.save(newVehicle);
                    });

            vehiclePolicy.setVehicle(vehicle);

            policy = vehiclePolicy;

        } else { // HEALTH (your LIFE equivalent)

            HealthPolicy healthPolicy = new HealthPolicy();

            healthPolicy.setCoverageType(aiDto.getCoverageType());
            healthPolicy.setCoverageAmount(aiDto.getCoverageAmount());
            healthPolicy.setBeneficiaryName(aiDto.getBeneficiaryName());
            healthPolicy.setInsuredPersonAge(
                    aiDto.getInsuredPersonAge() != null ? aiDto.getInsuredPersonAge() : 0
            );

            policy = healthPolicy;
        }

        // --- Common fields ---
        policy.setClient(client);
        policy.setPolicyNumber(aiDto.getPolicyNumber());
        policy.setInsuredName(
                aiDto.getInsuredName() != null ? aiDto.getInsuredName() : client.getName()
        );
        policy.setProvider(
                aiDto.getInsurerName() != null ? aiDto.getInsurerName() : "UNKNOWN"
        );
        policy.setType(policyType);
        policy.setStatus(PolicyStatus.ACTIVE);

        if (aiDto.getPremiumAmount() != null) {
            policy.setPremium(BigDecimal.valueOf(aiDto.getPremiumAmount()));
        } else {
            policy.setPremium(BigDecimal.ZERO);
        }

        parseDatesSafely(policy, aiDto);

        log.info("Saved {} policy with status {}",
                policyType,
                policy.getStatus());

        return policy;
    }

    private void parseDatesSafely(Policy policy, PolicyExtractionDTO aiDto) {

        try {
            if (aiDto.getEffectiveDate() != null)
                policy.setStartDate(LocalDate.parse(aiDto.getEffectiveDate()));
        } catch (Exception e) {
            log.warn("Invalid effective date: {}", aiDto.getEffectiveDate());
        }

        try {
            if (aiDto.getExpiryDate() != null)
                policy.setEndDate(LocalDate.parse(aiDto.getExpiryDate()));
        } catch (Exception e) {
            log.warn("Invalid expiry date: {}", aiDto.getExpiryDate());
        }
    }

    private void ingestMetadataToAI(Policy policy, String rawText, Long clientId) {

        Document springDoc = new Document(rawText);
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(List.of(springDoc));

        for (Document chunk : chunks) {
            chunk.getMetadata().putAll(Map.of(
                    "policyId", policy.getPolicyId(),
                    "clientId", clientId,
                    "policyNumber", policy.getPolicyNumber(),
                    "policyType", policy.getType().name(),
                    "source", "uploaded_file"
            ));
        }

        vectorStore.accept(chunks);
    }

    private void saveDocumentEntity(Policy policy, String name, String path) {
        com.athul.bhaang.Entity.Document docEntity =
                new com.athul.bhaang.Entity.Document();

        docEntity.setFileName(name);
        docEntity.setFilePath(path);
        docEntity.setPolicy(policy);

        documentRepository.save(docEntity);
    }

    private DocumentProcessingResponseDTO buildResponse(
            Policy policy,
            PolicyExtractionDTO aiDto
    ) {
        return DocumentProcessingResponseDTO.builder()
                .documentId(policy.getPolicyId())
                .status("PROCESSED & ROUTED")
                .confidenceScore(
                        aiDto.getConfidenceScore() != null
                                ? aiDto.getConfidenceScore()
                                : 0.0
                )
                .fileName(policy.getPolicyNumber())
                .extractedData(aiDto)
                .build();
    }
}