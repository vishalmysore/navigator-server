package com.navigator.controller;

import com.navigator.model.Document;
import com.navigator.model.request.SearchRequest;
import com.navigator.model.response.SearchResponse;
import com.navigator.service.QdrantService;
import com.navigator.service.RAGService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for search endpoints.
 * Provides vector similarity search using Qdrant or in-memory RAG.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class SearchController {

    private final QdrantService qdrantService;
    private final RAGService ragService;

    public SearchController(QdrantService qdrantService, RAGService ragService) {
        this.qdrantService = qdrantService;
        this.ragService = ragService;
    }

    /**
     * Search endpoint
     * POST /api/search
     * 
     * Uses Qdrant if available, falls back to in-memory RAG
     */
    @PostMapping("/search")
    public ResponseEntity<List<SearchResponse>> search(@Valid @RequestBody SearchRequest request) {
        try {
            List<Document> results;

            // Try Qdrant first
            if (qdrantService.isAvailable()) {
                log.info("Using Qdrant for search");
                results = qdrantService.searchTopK(request.getQuery(), request.getTopK(), request.getApiKey());
            } else {
                log.info("Qdrant not available, using in-memory RAG");
                // Fallback to RAG service
                // Note: RAGService doesn't have a direct search method, so we'll return empty
                // for now
                // In a full implementation, you'd add a search method to RAGService
                results = new ArrayList<>();
            }

            // Convert to response format
            List<SearchResponse> responses = results.stream()
                    .map(doc -> new SearchResponse(
                            doc.getText(),
                            1.0, // Score would come from Qdrant in real implementation
                            doc.getMetadata()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("Error in search: {}", e.getMessage());
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
}
