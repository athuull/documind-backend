package com.athul.documind.Service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class PDFParserService {

    // Keep your existing method
    public String extractText(MultipartFile file) throws IOException {
        return extractTextFromBytes(file.getBytes());
    }

    // NEW: Add this to handle reprocessing from saved files
    public String extractTextFromBytes(byte[] fileBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException("No extractable text found in PDF.");
            }

            return text.length() > 12000 ? text.substring(0, 12000) : text;
        }
    }
}