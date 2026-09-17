package com.athul.documind.Controller;

import com.athul.documind.DTO.ChatResponseDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public ChatController(ChatClient.Builder builder, VectorStore vectorStore) {

        this.vectorStore = vectorStore;

        SearchRequest searchRequest = SearchRequest.builder()
                .topK(6)
                .similarityThreshold(0.3)
                .build();

        var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .build();

        this.chatClient = builder
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
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        qaAdvisor
                )
                .build();
    }

    @GetMapping("/ask")
    public ResponseEntity<ChatResponseDTO> ask(
            @RequestParam String question,
            @RequestParam Long clientId,
            @RequestParam(required = false) Long policyId) {

        if (question == null || question.trim().isEmpty()) {
            return badRequest("Question cannot be empty");
        }

        if (clientId == null) {
            return badRequest("Client ID is required");
        }

        String filter = buildFilter(clientId, policyId);

        try {
            ChatResponse response = chatClient.prompt()
                    .user(question)
                    .advisors(a -> a.param(
                            QuestionAnswerAdvisor.FILTER_EXPRESSION,
                            filter
                    ))
                    .call()
                    .chatResponse();

            return ResponseEntity.ok(buildResponse(response));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Failed to process question: " + e.getMessage()));
        }
    }

    // ============== DEBUG ENDPOINTS ==============

    @GetMapping("/debug/vectors")
    public ResponseEntity<?> debugVectors(@RequestParam Long clientId) {
        try {
            var results = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query("insurance policy")
                            .topK(10)
                            .similarityThreshold(0.0)
                            .build()
            );

            return ResponseEntity.ok(Map.of(
                    "totalFound", results.size(),
                    "samples", results.stream()
                            .limit(5)
                            .map(doc -> Map.of(
                                    "metadata", doc.getMetadata(),
                                    "contentPreview", getContentPreview(doc, 200)
                            ))
                            .collect(Collectors.toList())
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/debug/filter")
    public ResponseEntity<?> debugFilter(
            @RequestParam Long clientId,
            @RequestParam(required = false) Long policyId) {

        try {
            String filter = buildFilter(clientId, policyId);

            var results = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query("policy coverage")
                            .topK(10)
                            .similarityThreshold(0.0)
                            .filterExpression(filter)
                            .build()
            );

            return ResponseEntity.ok(Map.of(
                    "filter", filter,
                    "resultsFound", results.size(),
                    "results", results.stream()
                            .map(doc -> Map.of(
                                    "metadata", doc.getMetadata(),
                                    "contentPreview", getContentPreview(doc, 150)
                            ))
                            .collect(Collectors.toList())
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/debug/retrieval")
    public ResponseEntity<?> debugRetrieval(
            @RequestParam String question,
            @RequestParam Long clientId,
            @RequestParam(required = false) Long policyId) {

        try {
            String filter = buildFilter(clientId, policyId);

            // Test without filter
            var unfilteredResults = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(question)
                            .topK(6)
                            .similarityThreshold(0.3)
                            .build()
            );

            // Test with filter
            var filteredResults = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(question)
                            .topK(6)
                            .similarityThreshold(0.3)
                            .filterExpression(filter)
                            .build()
            );

            return ResponseEntity.ok(Map.of(
                    "question", question,
                    "filter", filter,
                    "unfilteredCount", unfilteredResults.size(),
                    "filteredCount", filteredResults.size(),
                    "unfilteredSamples", unfilteredResults.stream().limit(3)
                            .map(d -> Map.of(
                                    "metadata", d.getMetadata(),
                                    "contentPreview", getContentPreview(d, 150)
                            ))
                            .collect(Collectors.toList()),
                    "filteredSamples", filteredResults.stream().limit(3)
                            .map(d -> Map.of(
                                    "metadata", d.getMetadata(),
                                    "contentPreview", getContentPreview(d, 150)
                            ))
                            .collect(Collectors.toList())
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ============== HELPER METHODS ==============

    private String buildFilter(Long clientId, Long policyId) {
        if (policyId != null) {
            return "clientId == " + clientId + " && policyId == " + policyId;
        }
        return "clientId == " + clientId;
    }

    private String getContentPreview(Document doc, int maxLength) {
        if (doc == null) {
            return "[No document]";
        }

        String content = doc.getFormattedContent();
        if (content == null || content.isEmpty()) {
            return "[No content]";
        }

        return content.substring(0, Math.min(maxLength, content.length()));
    }

    private ChatResponseDTO buildResponse(ChatResponse response) {
        ChatResponseDTO dto = new ChatResponseDTO();

        if (response == null || response.getResult() == null) {
            return errorResponse("No response from AI model.");
        }

        String answer = response.getResult().getOutput().getText();

        dto.setAnswer(
                (answer != null && !answer.trim().isEmpty())
                        ? answer
                        : "No relevant information found."
        );

        var usage = response.getMetadata().getUsage();
        dto.setTotalTokens(usage.getTotalTokens());
        dto.setPromptTokens(usage.getPromptTokens());
        dto.setCompletionTokens(usage.getCompletionTokens());

        return dto;
    }

    private ResponseEntity<ChatResponseDTO> badRequest(String message) {
        return ResponseEntity.badRequest().body(errorResponse(message));
    }

    private ChatResponseDTO errorResponse(String message) {
        ChatResponseDTO dto = new ChatResponseDTO();
        dto.setAnswer(message);
        dto.setTotalTokens(0);
        dto.setPromptTokens(0);
        dto.setCompletionTokens(0);
        return dto;
    }
}