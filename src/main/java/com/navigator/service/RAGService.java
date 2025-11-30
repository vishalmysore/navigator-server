package com.navigator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navigator.model.Document;
import com.navigator.util.EmbeddingUtil;
import com.navigator.util.TextSplitter;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval Augmented Generation) Service.
 * Manages document ingestion, embedding generation, and context-aware querying.
 */
@Slf4j
@Service
public class RAGService {

    private final OpenAIService openAIService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${storage.rag-index-file:/tmp/rag_index.json}")
    private String ragIndexFile;

    private List<Document> documents = new ArrayList<>();

    public RAGService(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    /**
     * Add a document to the RAG system with chunking
     */
    public void addDocument(String text, Map<String, Object> metadata, String apiKey) {
        // Split text into chunks
        TextSplitter splitter = new TextSplitter(1000, 200);
        List<String> chunks = splitter.splitText(text);

        log.info("Adding {} chunks to RAG system", chunks.size());

        for (String chunk : chunks) {
            // Generate embedding for each chunk
            List<Float> embedding = openAIService.createEmbedding(chunk, apiKey);

            // Create document with metadata
            Map<String, Object> chunkMetadata = new HashMap<>(metadata);
            chunkMetadata.put("chunk_index", documents.size());

            Document doc = new Document(chunk, embedding, chunkMetadata);
            documents.add(doc);
        }
    }

    /**
     * Query the RAG system and return context-aware response
     */
    public String query(String question, String apiKey) {
        if (documents.isEmpty()) {
            return "No documents have been added to the RAG system yet.";
        }

        // Get embedding for the question
        List<Float> questionEmbedding = openAIService.createEmbedding(question, apiKey);

        // Find most similar documents
        List<DocumentSimilarity> similarities = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            double similarity = EmbeddingUtil.cosineSimilarity(questionEmbedding, doc.getEmbedding());
            similarities.add(new DocumentSimilarity(i, similarity));
        }

        // Sort by similarity and get top 3
        similarities.sort((a, b) -> Double.compare(b.similarity, a.similarity));
        List<Document> topDocs = similarities.stream()
                .limit(3)
                .map(s -> documents.get(s.index))
                .collect(Collectors.toList());

        // Create context from top documents
        String context = topDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        // Generate response using OpenAI with context
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(
                "You are a helpful assistant. Answer the user's question based ONLY on the provided context. " +
                        "If the answer cannot be found in the context, say 'I cannot find the answer in the provided documents.'\n\n"
                        +
                        "Context:\n" + context));
        messages.add(UserMessage.from(question));

        return openAIService.chatCompletion(messages, apiKey);
    }

    /**
     * Get the number of documents in the system
     */
    public int getDocumentCount() {
        return documents.size();
    }

    /**
     * Save RAG state to file
     */
    public void saveState() {
        try {
            File file = new File(ragIndexFile);
            file.getParentFile().mkdirs();

            Map<String, Object> state = new HashMap<>();
            state.put("documents", documents);

            objectMapper.writeValue(file, state);
            log.info("📚 RAG state saved to {}", ragIndexFile);
        } catch (IOException e) {
            log.error("Error saving RAG state: {}", e.getMessage());
        }
    }

    /**
     * Load RAG state from file
     */
    public boolean loadState() {
        try {
            File file = new File(ragIndexFile);
            if (file.exists()) {
                Map<String, Object> state = objectMapper.readValue(file,
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                        });

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> docMaps = (List<Map<String, Object>>) state.get("documents");

                documents = new ArrayList<>();
                for (Map<String, Object> docMap : docMaps) {
                    Document doc = objectMapper.convertValue(docMap, Document.class);
                    documents.add(doc);
                }

                log.info("📚 RAG state loaded from {}. Documents: {}", ragIndexFile, documents.size());
                return true;
            } else {
                log.info("📚 No RAG state file found at {}", ragIndexFile);
                return false;
            }
        } catch (IOException e) {
            log.error("Error loading RAG state: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Clear all documents
     */
    public void clearDocuments() {
        documents.clear();
    }

    /**
     * Helper class for document similarity
     */
    private static class DocumentSimilarity {
        int index;
        double similarity;

        DocumentSimilarity(int index, double similarity) {
            this.index = index;
            this.similarity = similarity;
        }
    }
}
