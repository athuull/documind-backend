package com.athul.documind.Service;

import com.athul.documind.DTO.DocumentProcessingResponseDTO;
import com.athul.documind.DTO.PolicyExtractionDTO;
import com.athul.documind.Entity.*;
import com.athul.documind.Enum.PolicyStatus;
import com.athul.documind.Enum.PolicyType;
import com.athul.documind.Repository.ClientRepository;
import com.athul.documind.Repository.DocumentRepository;
import com.athul.documind.Repository.PolicyRepository;
import com.athul.documind.Repository.VehicleRepository;
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
        PolicyStatus initialStatus = PolicyStatus.ACTIVE;

        if (policyType == PolicyType.VEHICLE) {
            VehiclePolicy vehiclePolicy = new VehiclePolicy();
            vehiclePolicy.setVehicleRegistration(aiDto.getVehicleRegistration());
            vehiclePolicy.setVehicleType(aiDto.getVehicleType());
            vehiclePolicy.setCoverageType(aiDto.getCoverageType());

            String registration = aiDto.getVehicleRegistration();

            if (registration == null || registration.isBlank()) {
                log.warn("AI classified as VEHICLE but registration missing. Marking for manual review.");
                initialStatus = PolicyStatus.MANUAL_REVIEW;
            } else {
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
            }

            policy = vehiclePolicy;

        } else { // HEALTH
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
        policy.setPolicyNumber(
                aiDto.getPolicyNumber() != null && !aiDto.getPolicyNumber().isBlank()
                        ? aiDto.getPolicyNumber()
                        : "POL-" + System.currentTimeMillis()
        );
        policy.setInsuredName(
                aiDto.getInsuredName() != null && !aiDto.getInsuredName().isBlank()
                        ? aiDto.getInsuredName()
                        : client.getName()
        );
        policy.setProvider(
                aiDto.getInsurerName() != null && !aiDto.getInsurerName().isBlank()
                        ? aiDto.getInsurerName()
                        : "UNKNOWN"
        );
        policy.setType(policyType);
        policy.setStatus(initialStatus);

        if (aiDto.getPremiumAmount() != null) {
            policy.setPremium(BigDecimal.valueOf(aiDto.getPremiumAmount()));
        } else {
            policy.setPremium(BigDecimal.ZERO);
        }

        parseDatesSafely(policy, aiDto);

        log.info("Saved {} policy with status {}", policyType, policy.getStatus());

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
            java.util.HashMap<String, Object> meta = new java.util.HashMap<>();
            if (policy.getPolicyId() != null) meta.put("policyId", policy.getPolicyId());
            if (clientId != null) meta.put("clientId", clientId);
            meta.put("policyNumber", policy.getPolicyNumber() != null ? policy.getPolicyNumber() : "UNKNOWN");
            meta.put("policyType", policy.getType() != null ? policy.getType().name() : "UNKNOWN");
            meta.put("source", "uploaded_file");
            chunk.getMetadata().putAll(meta);
        }

        vectorStore.accept(chunks);
    }

    private com.athul.documind.Entity.Document saveDocumentEntity(Policy policy, String name, String path) {
        com.athul.documind.Entity.Document docEntity =
                new com.athul.documind.Entity.Document();

        docEntity.setFileName(name);
        docEntity.setFilePath(path);
        docEntity.setPolicy(policy);
        docEntity.setProcessed(true);
        docEntity.setExtractedAt(java.time.LocalDateTime.now());

        return documentRepository.save(docEntity);
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