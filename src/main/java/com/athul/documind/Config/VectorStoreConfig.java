package com.athul.documind.Config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // 1. Build the store and assign it to a variable
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // 2. Define your persistence file
        File storageFile = new File("src/main/resources/vector-store.json");

        // 3. Load data if the file exists
        if (storageFile.exists()) {
            vectorStore.load(storageFile);
        }

        // 4. Finally, return the configured bean
        return vectorStore;
    }
}