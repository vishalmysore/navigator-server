package com.navigator.model.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Response model for RAG status endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RAGStatusResponse {
    private boolean hasIndex;
    private int documentsCount;
    private String status;
    private String message;
}
