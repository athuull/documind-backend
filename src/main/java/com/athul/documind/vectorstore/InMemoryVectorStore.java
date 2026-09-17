package com.athul.documind.vectorstore;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryVectorStore {

    private final List<VectorDocument> storage = new ArrayList<>();
    private final EmbeddingModel embeddingModel;

    public InMemoryVectorStore(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public void add(String id, Map<String, String> metadata, String text) {
        // Automatically generate the float[] embedding from the text
        float[] embedding = embeddingModel.embed(text);
        storage.add(new VectorDocument(id, metadata, embedding));
    }

    public List<VectorDocument> search(String query, String policyId, int topK, double threshold) {
        float[] queryVector = embeddingModel.embed(query);

        return storage.stream()
                // Filter by policyId if provided
                .filter(doc -> policyId == null || policyId.equals(doc.metadata().get("policyId")))
                // Sort by similarity (Simple Dot Product or Cosine Similarity)
                .sorted((a, b) -> Double.compare(cosineSimilarity(b.embedding(), queryVector),
                        cosineSimilarity(a.embedding(), queryVector)))
                .limit(topK)
                .collect(Collectors.toList());
    }

    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public List<VectorDocument> getDocuments() {
        return Collections.unmodifiableList(storage);
    }
}