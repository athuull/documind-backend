package com.athul.bhaang.Service;

import com.athul.bhaang.DTO.PolicyExtractionDTO;
import com.athul.bhaang.Entity.Client;
import com.athul.bhaang.Enum.PolicyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
@Slf4j
@RequiredArgsConstructor
public class PolicyExtractionService {

    private final ChatClient chatClient;
    private final PDFParserService pdfParserService;

    /**
     * Extract policy data from PDF using structured AI output.
     * AI MUST classify strictly as HEALTH or VEHICLE.
     */
    public PolicyExtractionDTO extractPolicyData(MultipartFile file, Client client) throws IOException {

        String fileName = file.getOriginalFilename();
        String rawText = pdfParserService.extractText(file);
        String cleanedText = rawText.replaceAll("\\s+", " ").trim();

        BeanOutputConverter<PolicyExtractionDTO> converter =
                new BeanOutputConverter<>(PolicyExtractionDTO.class);

        try {
            log.info("Starting AI extraction for clientId={} - fileName={}",
                    client.getId(), fileName);

            PolicyExtractionDTO aiDto = chatClient.prompt()
                    .system(sp -> sp.text("""
                            You are an expert insurance document classifier and extractor.

                            Your tasks:
                            1. Classify the policy strictly as ONE of:
                               - HEALTH
                               - VEHICLE

                            2. Extract all relevant details.

                            Rules:
                            - You MUST choose either HEALTH or VEHICLE.
                            - Do NOT invent other types.
                            - Do NOT return null for policyType.
                            - Use YYYY-MM-DD for dates.
                            - Remove currency symbols from numbers.
                            - If a field is missing, return null.
                            - Return ONLY valid JSON matching the provided schema.
                            """))
                    .user(u -> u.text("""
                            Extract policy details from the following document:

                            {text}

                            {format}
                            """)
                            .param("text", cleanedText)
                            .param("format", converter.getFormat()))
                    .call()
                    .entity(converter);

            // Store raw text for indexing
            aiDto.setRawText(rawText);

            // 🔥 STRICT VALIDATION — NO FALLBACKS
            if (aiDto.getPolicyType() == null) {
                throw new IllegalStateException(
                        "AI failed to classify policy type as LIFE or VEHICLE.");
            }

            if (aiDto.getPolicyType() != PolicyType.HEALTH &&
                    aiDto.getPolicyType() != PolicyType.VEHICLE) {
                throw new IllegalStateException(
                        "AI returned unsupported policy type: " + aiDto.getPolicyType());
            }

            return aiDto;

        } catch (Exception e) {
            log.error("AI extraction failed for clientId={} - fileName={}: {}",
                    client.getId(), fileName, e.getMessage());

            throw new IllegalStateException(
                    "Policy extraction failed. Manual review required.", e);
        }
    }

    /**
     * Computes SHA-256 hash of uploaded file
     */
    public String computeFileHash(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            log.error("Error computing file hash: {}", e.getMessage());
            throw new IOException("Failed to compute file hash", e);
        }
    }
}