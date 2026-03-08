package com.athul.bhaang.Service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public ChatService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.vectorStore = vectorStore;

        var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(6)
                        .similarityThreshold(0.3)
                        .build())
                .build();

        this.chatClient = builder
                .defaultAdvisors(new SimpleLoggerAdvisor(), qaAdvisor)
                .defaultSystem("""
                        You are a professional insurance assistant.
                        Answer questions using ONLY the provided policy context.

                        Rules:
                        1. Use only information from the context provided
                        2. If information is missing, say:
                           "I don't have that information in your policy document"
                        3. Always cite policy numbers when available
                        4. If multiple policies appear, separate clearly
                        5. Use bullet points for coverage amounts with ₹ symbol
                        6. Be concise and accurate

                        Never use general insurance knowledge.
                        """)
                .build();
    }

    public String chatWithPolicy(String userQuery, Long clientId) {
        String filter = "clientId == " + clientId;

        return this.chatClient.prompt()
                .user(userQuery)
                .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, filter))
                .call()
                .content();
    }

    public String chatWithSpecificPolicy(String userQuery, Long clientId, Long policyId) {
        String filter = String.format("clientId == %d && policyId == %d", clientId, policyId);

        return this.chatClient.prompt()
                .user(userQuery)
                .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, filter))
                .call()
                .content();
    }

    public int testRetrieval(String query, Long clientId, Long policyId) {
        String filter = policyId != null
                ? String.format("clientId == %d && policyId == %d", clientId, policyId)
                : "clientId == " + clientId;

        var results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(6)
                        .similarityThreshold(0.3)
                        .filterExpression(filter)
                        .build()
        );

        System.out.println("=== Test Retrieval ===");
        System.out.println("Query: " + query);
        System.out.println("Filter: " + filter);
        System.out.println("Results found: " + results.size());

        results.forEach(doc -> {
            System.out.println("Metadata: " + doc.getMetadata());

            String content = doc.getFormattedContent();
            String preview = !content.isEmpty()
                    ? content.substring(0, Math.min(100, content.length()))
                    : "[No content]";
            System.out.println("Content preview: " + preview);
            System.out.println("---");
        });

        return results.size();
    }
}