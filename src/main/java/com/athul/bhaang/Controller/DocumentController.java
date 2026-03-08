package com.athul.bhaang.Controller;

import com.athul.bhaang.DTO.DocumentProcessingResponseDTO;
import com.athul.bhaang.Entity.Document;
import com.athul.bhaang.Repository.DocumentRepository;
import com.athul.bhaang.Service.DocumentProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewFile(@PathVariable Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        Path path = Paths.get(doc.getFilePath());
        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .body(resource);
    }


}