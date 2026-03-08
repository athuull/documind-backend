package com.athul.bhaang.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentProcessingResponseDTO {

    private Long documentId;           // ID of the saved DB record
    private String fileName;
    private String status;             // PROCESSED, FAILED, PENDING_REVIEW
    private String message;
    private Double confidenceScore;
    private PolicyExtractionDTO extractedData;
    private Long clientId;


}