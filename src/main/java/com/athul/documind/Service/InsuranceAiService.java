package com.athul.documind.Service;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InsuranceAiService {

    private final VectorStore vectorStore;

    public InsuranceAiService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void ingestPolicy(Map<String, Object> extractedData, Long documentId, Long clientId) {
        if (extractedData == null) {
            return;
        }

        // Construct content string for embeddings
        StringBuilder content = new StringBuilder();
        if (extractedData.containsKey("policyNumber")) content.append("Policy Number: ").append(extractedData.get("policyNumber")).append("\n");
        if (extractedData.containsKey("policyHolderName")) content.append("Policy Holder: ").append(extractedData.get("policyHolderName")).append("\n");
        if (extractedData.containsKey("insurerName")) content.append("Provider: ").append(extractedData.get("insurerName")).append("\n");
        if (extractedData.containsKey("effectiveDate")) content.append("Effective Date: ").append(extractedData.get("effectiveDate")).append("\n");
        if (extractedData.containsKey("expiryDate")) content.append("Expiry Date: ").append(extractedData.get("expiryDate")).append("\n");
        if (extractedData.containsKey("premiumAmount")) content.append("Premium Amount: ").append(extractedData.get("premiumAmount")).append("\n");

        Object coverageTypes = extractedData.get("coverageTypes");
        if (coverageTypes instanceof List<?> list) {
            content.append("Coverage Types: ").append(String.join(", ", list.stream().map(Object::toString).toList())).append("\n");
        }

        Map<String, Object> metadata = new HashMap<>();
        if (documentId != null) metadata.put("documentId", documentId);
        if (clientId != null) metadata.put("clientId", clientId);
        if (extractedData.get("policyNumber") != null) metadata.put("policyNumber", extractedData.get("policyNumber"));
        if (extractedData.get("policyHolderName") != null) metadata.put("policyHolderName", extractedData.get("policyHolderName"));
        if (extractedData.get("expiryDate") != null) metadata.put("expiryDate", extractedData.get("expiryDate"));
        if (extractedData.get("premiumAmount") != null) metadata.put("premiumAmount", extractedData.get("premiumAmount"));

        Document aiDoc = new Document(content.toString(), metadata);

        // Split into chunks
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(List.of(aiDoc));

        // Add chunks to vector store
        vectorStore.accept(chunks);
    }

    public void upsertPolicy(Map<String, Object> metadata, Long documentId, Long clientId) {
        try {
            // Delete existing vectors for this policyId
            vectorStore.delete(new FilterExpressionBuilder()
                    .eq("policyId", documentId)
                    .build());
        } catch (Exception e) {
            // Safe to ignore if no previous vectors exist or delete by filter unsupported
        }

        Document doc = new Document("");
        if (metadata != null) {
            doc.getMetadata().putAll(metadata);
        }
        if (clientId != null) doc.getMetadata().put("clientId", clientId);
        if (documentId != null) doc.getMetadata().put("policyId", documentId);

        vectorStore.accept(List.of(doc));
    }
}