package com.navigator.controller;

import com.navigator.model.ConversationMessage;
import com.navigator.model.request.RAGChatRequest;
import com.navigator.model.response.RAGResponse;
import com.navigator.model.response.RAGStatusResponse;
import com.navigator.service.ConversationService;
import com.navigator.service.RAGService;
import com.navigator.util.PDFProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.File;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for RAG (Retrieval Augmented Generation) endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "RAG", description = "Retrieval-Augmented Generation endpoints for document upload and querying")
public class RAGController {

    private final RAGService ragService;
    private final ConversationService conversationService;
    
    @Value("${openai.api-key}")
    private String openaiApiKey;

    public RAGController(RAGService ragService, ConversationService conversationService) {
        this.ragService = ragService;
        this.conversationService = conversationService;
    }
    
    /**
     * Upload PDF documents to the RAG system
     * POST /api/rag/upload
     */
    @PostMapping("/rag/upload")
    @Operation(summary = "Upload PDF documents", description = "Upload one or more PDF files to be indexed in the RAG system")
    public ResponseEntity<Map<String, Object>> uploadDocuments(
            @RequestParam("files") MultipartFile[] files) {
        try {
            int processedFiles = 0;
            int totalChunks = 0;
            
            for (MultipartFile file : files) {
                if (file.isEmpty() || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                    continue;
                }
                
                // Save temporary file
                File tempFile = File.createTempFile("upload_", ".pdf");
                file.transferTo(tempFile);
                
                try {
                    // Extract text from PDF
                    Map<String, Object> pdfData = PDFProcessor.extractTextWithMetadata(tempFile);
                    String text = (String) pdfData.get("text");
                    
                    // Prepare metadata
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("filename", file.getOriginalFilename());
                    metadata.put("pages", pdfData.get("pages"));
                    metadata.put("upload_time", Instant.now().toString());
                    
                    // Add to RAG system
                    int docCountBefore = ragService.getDocumentCount();
                    ragService.addDocument(text, metadata, openaiApiKey);
                    int chunksAdded = ragService.getDocumentCount() - docCountBefore;
                    
                    totalChunks += chunksAdded;
                    processedFiles++;
                    
                    log.info("✅ Processed: {} ({} chunks)", file.getOriginalFilename(), chunksAdded);
                } finally {
                    tempFile.delete();
                }
            }
            
            // Save state
            ragService.saveState();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("files_processed", processedFiles);
            response.put("chunks_added", totalChunks);
            response.put("total_chunks", ragService.getDocumentCount());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error uploading documents: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * RAG chat endpoint
     * POST /api/rag-chat
     */
    @PostMapping("/rag-chat")
    @Operation(summary = "RAG Chat", description = "Ask questions based on uploaded documents")
    public ResponseEntity<RAGResponse> ragChat(@Valid @RequestBody RAGChatRequest request) {
        try {
            // Add user message to conversation history
            ConversationMessage userMsg = new ConversationMessage(
                    "user",
                    request.getUserMessage(),
                    Instant.now().toString());
            conversationService.addMessage(request.getUserId(), userMsg);

            // Query RAG system
            String response = ragService.query(request.getUserMessage(), request.getApiKey());

            // Add assistant response to conversation history
            ConversationMessage assistantMsg = new ConversationMessage(
                    "assistant",
                    response,
                    Instant.now().toString());
            conversationService.addMessage(request.getUserId(), assistantMsg);

            // Return response
            return ResponseEntity.ok(new RAGResponse(
                    response,
                    ragService.getDocumentCount(),
                    "ok"));

        } catch (Exception e) {
            log.error("Error in RAG chat: {}", e.getMessage());
            return ResponseEntity.status(500).body(new RAGResponse(
                    "Error: " + e.getMessage(),
                    0,
                    "error"));
        }
    }

    /**
     * Get RAG system status
     * GET /api/rag-status
     */
    @GetMapping("/rag-status")
    @Operation(summary = "Get RAG status", description = "Check the current status of the RAG system")
    public ResponseEntity<RAGStatusResponse> getRagStatus() {
        try {
            int docCount = ragService.getDocumentCount();
            boolean hasIndex = docCount > 0;
            String status = hasIndex ? "ready" : "empty";
            String message = hasIndex
                    ? String.format("RAG system ready with %d document chunks", docCount)
                    : "No documents indexed yet. Upload PDFs to get started.";

            return ResponseEntity.ok(new RAGStatusResponse(
                    hasIndex,
                    docCount,
                    status,
                    message));

        } catch (Exception e) {
            log.error("Error checking RAG status: {}", e.getMessage());
            return ResponseEntity.ok(new RAGStatusResponse(
                    false,
                    0,
                    "error",
                    "Error checking RAG status: " + e.getMessage()));
        }
    }

    /**
     * Get RAG status for a specific user
     * GET /api/rag-status/{userId}
     */
    @GetMapping("/rag-status/{userId}")
    public ResponseEntity<RAGStatusResponse> getRagStatusByUser(@PathVariable String userId) {
        // For now, return global RAG status
        // In future, implement per-user RAG indices
        return getRagStatus();
    }
}
