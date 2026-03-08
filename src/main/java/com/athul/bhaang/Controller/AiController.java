package com.athul.bhaang.Controller;

import com.athul.bhaang.DTO.DocumentProcessingResponseDTO;
import com.athul.bhaang.Service.InsuranceAiService;
import com.athul.bhaang.Service.DocumentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final DocumentProcessingService documentProcessingService;
    private final InsuranceAiService insuranceAiService;

    /**
     * Upload and process an insurance policy document
     */
    @PostMapping("/client/{clientId}/upload")
    public ResponseEntity<DocumentProcessingResponseDTO> uploadPolicy(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long clientId) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("File cannot be empty"));
        }

        if (clientId == null || clientId <= 0) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid client ID"));
        }

        try {
            log.info("Uploading policy for clientId: {}, filename: {}", clientId, file.getOriginalFilename());

            // Process policy
            DocumentProcessingResponseDTO dto = documentProcessingService.processPolicyUpload(file, clientId);

            // Vector ingestion (upsert)
            if ("PROCESSED & INDEXED".equals(dto.getStatus()) && dto.getExtractedData() instanceof Map) {
                log.info("Ingesting policy into VectorStore - documentId: {}, clientId: {}",
                        dto.getDocumentId(), clientId);
                insuranceAiService.upsertPolicy((Map<String, Object>) dto.getExtractedData(),
                        dto.getDocumentId(), clientId);
            }

            log.info("Policy upload completed - documentId: {}", dto.getDocumentId());
            return ResponseEntity.ok(dto);

        } catch (IOException e) {
            log.error("IO error processing upload for clientId: {}", clientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to process file: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error processing upload for clientId: {}", clientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An unexpected error occurred: " + e.getMessage()));
        }
    }

    // --- Error DTO helper ---
    private DocumentProcessingResponseDTO createErrorResponse(String message) {
        DocumentProcessingResponseDTO dto = new DocumentProcessingResponseDTO();
        dto.setStatus("ERROR");
        dto.setMessage(message);
        return dto;
    }
}