package com.athul.documind.DTO;

import lombok.Data;

@Data
public class DocumentUploadRequestDTO {

    private String documentCategory;   // e.g. "POLICY", "CLAIM", "MEDICAL"
    private String uploadedBy;         // user or system identifier
    private String notes;              // any manual notes from the operator
}