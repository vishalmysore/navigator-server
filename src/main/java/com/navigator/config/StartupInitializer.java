package com.navigator.config;

import com.navigator.service.RAGService;
import com.navigator.util.PDFProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Startup initializer for the Navigator application.
 * Loads RAG state and performs other initialization tasks.
 */
@Slf4j
@Component
public class StartupInitializer {

    private final RAGService ragService;
    private final OpenAIConfig openAIConfig;
    
    @Value("${storage.knowledge-base-path:knowledge}")
    private String knowledgeBasePath;

    public StartupInitializer(RAGService ragService, OpenAIConfig openAIConfig) {
        this.ragService = ragService;
        this.openAIConfig = openAIConfig;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("🔄 Initializing Navigator application...");

        // Try to load existing RAG state first
        boolean loaded = ragService.loadState();
        if (loaded) {
            log.info("✅ RAG system initialized with {} document chunks from saved state", ragService.getDocumentCount());
        } else {
            log.info("📚 No saved RAG state found. Loading from knowledge base...");
            loadKnowledgeBase();
        }

        log.info("✅ Navigator application ready");
    }
    
    private void loadKnowledgeBase() {
        try {
            Path knowledgePath = Paths.get(knowledgeBasePath);
            
            if (!Files.exists(knowledgePath)) {
                log.warn("⚠️  Knowledge base path not found: {}. RAG system started empty.", knowledgeBasePath);
                return;
            }
            
            log.info("📂 Scanning knowledge base at: {}", knowledgePath.toAbsolutePath());
            
            int totalFiles = 0;
            int processedFiles = 0;
            int totalChunks = 0;
            
            // Walk through all PDF files in the knowledge directory
            try (Stream<Path> paths = Files.walk(knowledgePath)) {
                for (Path path : paths.filter(p -> p.toString().toLowerCase().endsWith(".pdf")).toList()) {
                    totalFiles++;
                    try {
                        File pdfFile = path.toFile();
                        
                        // Extract grade and subject from path
                        String relativePath = knowledgePath.relativize(path).toString();
                        Map<String, Object> metadata = extractMetadataFromPath(relativePath, pdfFile.getName());
                        
                        // Extract text from PDF
                        Map<String, Object> pdfData = PDFProcessor.extractTextWithMetadata(pdfFile);
                        String text = (String) pdfData.get("text");
                        
                        // Merge metadata
                        metadata.putAll(pdfData);
                        metadata.put("source", "knowledge_base");
                        metadata.put("loaded_at", Instant.now().toString());
                        
                        // Add to RAG system
                        int docCountBefore = ragService.getDocumentCount();
                        ragService.addDocument(text, metadata, openAIConfig.getApiKey());
                        int chunksAdded = ragService.getDocumentCount() - docCountBefore;
                        
                        totalChunks += chunksAdded;
                        processedFiles++;
                        
                        log.info("✅ Loaded: {} ({} chunks) - Grade {}, Subject: {}", 
                            pdfFile.getName(), 
                            chunksAdded,
                            metadata.get("grade"),
                            metadata.get("subject"));
                            
                    } catch (Exception e) {
                        log.error("❌ Failed to process {}: {}", path.getFileName(), e.getMessage());
                    }
                }
            }
            
            if (processedFiles > 0) {
                // Save the loaded state
                ragService.saveState();
                log.info("🎉 Knowledge base loaded: {} of {} files processed, {} total chunks", 
                    processedFiles, totalFiles, totalChunks);
            } else {
                log.warn("⚠️  No PDF files found in knowledge base at: {}", knowledgePath.toAbsolutePath());
            }
            
        } catch (Exception e) {
            log.error("❌ Error loading knowledge base: {}", e.getMessage(), e);
        }
    }
    
    private Map<String, Object> extractMetadataFromPath(String relativePath, String filename) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("filename", filename);
        metadata.put("path", relativePath);
        
        // Parse path like: grades/3/science/filename.pdf
        String[] parts = relativePath.replace("\\", "/").split("/");
        
        if (parts.length >= 3 && parts[0].equals("grades")) {
            metadata.put("grade", parts[1]);
            metadata.put("subject", parts[2]);
        } else {
            metadata.put("grade", "unknown");
            metadata.put("subject", "unknown");
        }
        
        return metadata;
    }
}
