package com.athul.documind.Controller;

import com.athul.documind.DTO.DocumentProcessingResponseDTO;
import com.athul.documind.Entity.Document;
import com.athul.documind.Repository.DocumentRepository;
import com.athul.documind.Service.DocumentProcessingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentProcessingService documentProcessingService;
    private final DocumentRepository documentRepository;

    @PostMapping(value = "/client/{clientId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentProcessingResponseDTO> upload(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long clientId) throws IOException {

        DocumentProcessingResponseDTO response = documentProcessingService.processPolicyUpload(file, clientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewFile(@PathVariable Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + id));

        Path path = Paths.get(doc.getFilePath());
        if (!Files.exists(path)) {
            throw new EntityNotFoundException("Document file not found on disk at: " + doc.getFilePath());
        }

        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<DocumentSummaryDTO>> getDocumentsByClient(@PathVariable Long clientId) {
        List<Document> docs = documentRepository.findByPolicyClientId(clientId);
        return ResponseEntity.ok(docs.stream().map(this::mapToSummary).toList());
    }

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<DocumentSummaryDTO>> getDocumentsByPolicy(@PathVariable Long policyId) {
        List<Document> docs = documentRepository.findByPolicyId(policyId);
        return ResponseEntity.ok(docs.stream().map(this::mapToSummary).toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + id));

        try {
            Files.deleteIfExists(Paths.get(doc.getFilePath()));
        } catch (Exception ignored) {}

        documentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private DocumentSummaryDTO mapToSummary(Document doc) {
        return DocumentSummaryDTO.builder()
                .id(doc.getId())
                .fileName(doc.getFileName())
                .uploadDate(doc.getUploadDate())
                .processed(doc.isProcessed())
                .extractedAt(doc.getExtractedAt())
                .policyId(doc.getPolicy() != null ? doc.getPolicy().getPolicyId() : null)
                .policyNumber(doc.getPolicy() != null ? doc.getPolicy().getPolicyNumber() : null)
                .build();
    }

    @Data
    @Builder
    public static class DocumentSummaryDTO {
        private Long id;
        private String fileName;
        private LocalDateTime uploadDate;
        private boolean processed;
        private LocalDateTime extractedAt;
        private Long policyId;
        private String policyNumber;
    }
}