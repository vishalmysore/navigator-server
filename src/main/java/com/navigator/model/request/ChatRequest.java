package com.navigator.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request model for chat endpoint.
 * Equivalent to Python's ChatRequest Pydantic model.
 */
@Data
public class ChatRequest {

    @NotBlank(message = "Developer message is required")
    private String developerMessage;

    @NotBlank(message = "User message is required")
    private String userMessage;

    private String model = "gpt-4o-mini"; // Default model

    @NotBlank(message = "API key is required")
    private String apiKey;

    @NotBlank(message = "User ID is required")
    private String userId;
}
