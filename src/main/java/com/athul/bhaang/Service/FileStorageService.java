package com.athul.bhaang.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root = Paths.get("uploads").toAbsolutePath().normalize();

    public String saveFile(MultipartFile file) throws IOException {
        // Ensure the upload directory exists
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }

        // Generate a unique filename to prevent collisions
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IOException("File must have an original filename");
        }
        String fileName = UUID.randomUUID() + "_" + originalFileName;

        Path targetLocation = root.resolve(fileName);

        // Copy the file to the target location (overwrite if exists)
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return targetLocation.toString();
    }

    public Path getRootPath() {
        return root;
    }
}