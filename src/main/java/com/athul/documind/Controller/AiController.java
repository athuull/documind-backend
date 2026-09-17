package com.athul.documind.Controller;

import com.athul.documind.DTO.DocumentProcessingResponseDTO;
import com.athul.documind.Service.InsuranceAiService;
import com.athul.documind.Service.DocumentProcessingService;
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

            // Process policy and ingest metadata (handled by documentProcessingService)
            DocumentProcessingResponseDTO dto = documentProcessingService.processPolicyUpload(file, clientId);

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