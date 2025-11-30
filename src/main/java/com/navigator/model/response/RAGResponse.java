package com.navigator.model.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Response model for RAG chat endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RAGResponse {
    private String message;
    private int documentsCount;
    private String status;
}
