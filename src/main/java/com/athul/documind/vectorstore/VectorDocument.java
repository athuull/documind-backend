package com.athul.documind.vectorstore;

import java.util.Map;

public record VectorDocument(String id, Map<String, String> metadata, float[] embedding) {
    // Using a record for conciseness; it includes getters, equals, and hashCode
}