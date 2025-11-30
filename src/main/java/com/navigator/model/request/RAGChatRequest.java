package com.navigator.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request model for RAG chat endpoint.
 */
@Data
public class RAGChatRequest {

    @NotBlank(message = "User message is required")
    private String userMessage;

    private String model = "gpt-4o-mini";

    @NotBlank(message = "API key is required")
    private String apiKey;

    @NotBlank(message = "User ID is required")
    private String userId;
}
