package com.navigator.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Request model for search endpoint.
 */
@Data
public class SearchRequest {

    @NotBlank(message = "Query is required")
    private String query;

    @Min(value = 1, message = "top_k must be at least 1")
    private int topK = 4;

    @NotBlank(message = "API key is required")
    private String apiKey;
}
