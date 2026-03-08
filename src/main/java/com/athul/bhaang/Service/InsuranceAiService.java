package com.athul.bhaang.Service;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class InsuranceAiService {

    private final VectorStore vectorStore;

    public InsuranceAiService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void ingestPolicy(Map<String, Object> extractedData, Long documentId, Long clientId) {
        // Construct content string for embeddings
        StringBuilder content = new StringBuilder();
        content.append("Policy Number: ").append(extractedData.get("policyNumber")).append("\n");
        content.append("Policy Holder: ").append(extractedData.get("policyHolderName")).append("\n");
        content.append("Provider: ").append(extractedData.get("insurerName")).append("\n");
        content.append("Effective Date: ").append(extractedData.get("effectiveDate")).append("\n");
        content.append("Expiry Date: ").append(extractedData.get("expiryDate")).append("\n");
        content.append("Premium Amount: ").append(extractedData.get("premiumAmount")).append("\n");
        content.append("Coverage Types: ").append(String.join(", ", (List<String>) extractedData.get("coverageTypes"))).append("\n");

        Document aiDoc = new Document(content.toString(), Map.of(
                "documentId", documentId,
                "clientId", clientId,
                "policyNumber", extractedData.get("policyNumber"),
                "policyHolderName", extractedData.get("policyHolderName"),
                "expiryDate", extractedData.get("expiryDate"),
                "premiumAmount", extractedData.get("premiumAmount")
        ));

        // Split into chunks
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(List.of(aiDoc));

        // Add chunks to vector store
        vectorStore.add(chunks);
    }

    public void upsertPolicy(Map<String, Object> metadata, Long documentId, Long clientId) {
        try {
            // Delete existing vectors for this policyId
            vectorStore.delete(new FilterExpressionBuilder()
                    .eq("policyId", documentId)
                    .build());
        } catch (Exception e) {
            // Safe to ignore if no previous vectors exist
        }

        // Convert metadata to a Document object
        Document doc = new Document(""); // empty content, metadata carries policy info
        doc.getMetadata().putAll(metadata);
        doc.getMetadata().put("clientId", clientId);
        doc.getMetadata().put("policyId", documentId);

        // Accept document into VectorStore
        vectorStore.accept(List.of(doc));
    }
}