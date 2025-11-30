package com.navigator.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request model for evaluation endpoint.
 */
@Data
public class EvaluateRequest {

    @NotBlank(message = "Question is required")
    private String question;

    @NotBlank(message = "Answer is required")
    private String answer;

    private String context = "";

    @NotBlank(message = "API key is required")
    private String apiKey;
}
