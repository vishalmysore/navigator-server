package com.navigator.service;

import com.navigator.config.OpenAIConfig;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.ArrayList;

/**
 * Service for OpenAI API interactions.
 * Handles chat completions, streaming, and embeddings.
 */
@Slf4j
@Service
public class OpenAIService {

    private final OpenAIConfig config;

    public OpenAIService(OpenAIConfig config) {
        this.config = config;
    }

    /**
     * Create a chat language model with the given API key
     */
    private ChatLanguageModel createChatModel(String apiKey) {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(config.getModel())
                .maxTokens(config.getMaxTokens())
                .build();
    }

    /**
     * Create an embedding model with the given API key
     */
    private OpenAiEmbeddingModel createEmbeddingModel(String apiKey) {
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(config.getEmbeddingModel())
                .build();
    }

    /**
     * Generate a chat completion (non-streaming)
     */
    public String chatCompletion(List<ChatMessage> messages, String apiKey) {
        try {
            ChatLanguageModel model = createChatModel(apiKey);
            AiMessage response = model.generate(messages).content();
            return response.text();
        } catch (Exception e) {
            log.error("Error generating chat completion: {}", e.getMessage());
            throw new RuntimeException("Error generating chat completion: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a streaming chat completion
     * Returns a Flux of text chunks
     * 
     * Note: Simplified implementation - returns complete response as single chunk
     * Full streaming will be implemented with proper LangChain4j streaming API
     */
    public Flux<String> streamChatCompletion(List<ChatMessage> messages, String apiKey) {
        return Flux.create(sink -> {
            try {
                String response = chatCompletion(messages, apiKey);
                // For now, send the complete response as a single chunk
                // TODO: Implement proper streaming with LangChain4j streaming API
                sink.next(response);
                sink.complete();
            } catch (Exception e) {
                log.error("Error in streaming chat: {}", e.getMessage());
                sink.error(e);
            }
        });
    }

    /**
     * Generate embedding for text
     */
    public List<Float> createEmbedding(String text, String apiKey) {
        try {
            OpenAiEmbeddingModel embeddingModel = createEmbeddingModel(apiKey);
            Embedding embedding = embeddingModel.embed(text).content();

            // Convert float[] to List<Float>
            float[] vector = embedding.vector();
            List<Float> result = new ArrayList<>(vector.length);
            for (float v : vector) {
                result.add(v);
            }
            return result;
        } catch (Exception e) {
            log.error("Error generating embedding: {}", e.getMessage());
            // Return default embedding size (1536 for text-embedding-3-small)
            List<Float> defaultEmbedding = new ArrayList<>(1536);
            for (int i = 0; i < 1536; i++) {
                defaultEmbedding.add(0.0f);
            }
            return defaultEmbedding;
        }
    }

    /**
     * Helper method to create a system message
     */
    public SystemMessage createSystemMessage(String content) {
        return SystemMessage.from(content);
    }

    /**
     * Helper method to create a user message
     */
    public UserMessage createUserMessage(String content) {
        return UserMessage.from(content);
    }
}
