package com.navigator.controller;

import com.navigator.model.ConversationMessage;
import com.navigator.model.request.ChatRequest;
import com.navigator.model.response.ConversationResponse;
import com.navigator.service.ConversationService;
import com.navigator.service.OpenAIService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for chat endpoints.
 * Handles chat interactions with streaming support.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ChatController {

    private final OpenAIService openAIService;
    private final ConversationService conversationService;

    public ChatController(OpenAIService openAIService, ConversationService conversationService) {
        this.openAIService = openAIService;
        this.conversationService = conversationService;
    }

    /**
     * Chat endpoint with streaming response
     * POST /api/chat
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> chat(@Valid @RequestBody ChatRequest request) {
        try {
            // Add user message to conversation history
            ConversationMessage userMsg = new ConversationMessage(
                    "user",
                    request.getUserMessage(),
                    Instant.now().toString());
            conversationService.addMessage(request.getUserId(), userMsg);

            // Get last 10 messages for context
            List<ConversationMessage> history = conversationService.getLastNMessages(
                    request.getUserId(), 10);

            // Prepare messages for OpenAI
            List<ChatMessage> messages = new ArrayList<>();

            // Add system message
            String systemPrompt = "You are a helpful AI assistant. Always provide clear, accurate, " +
                    "and well-structured responses. When explaining concepts, use simple language and " +
                    "relatable examples. When summarizing, capture all key points concisely. When writing " +
                    "creatively, be imaginative and engaging. When solving problems, show your reasoning " +
                    "step-by-step. When rewriting text, maintain professional tone and correct all errors.";
            messages.add(SystemMessage.from(systemPrompt));

            // Add conversation history
            for (ConversationMessage msg : history) {
                if ("user".equals(msg.getRole())) {
                    messages.add(UserMessage.from(msg.getContent()));
                } else if ("assistant".equals(msg.getRole())) {
                    messages.add(dev.langchain4j.data.message.AiMessage.from(msg.getContent()));
                }
            }

            // Stream the response and collect it
            StringBuilder fullResponse = new StringBuilder();

            return openAIService.streamChatCompletion(messages, request.getApiKey())
                    .doOnNext(fullResponse::append)
                    .doOnComplete(() -> {
                        // Save assistant response to conversation history
                        ConversationMessage assistantMsg = new ConversationMessage(
                                "assistant",
                                fullResponse.toString(),
                                Instant.now().toString());
                        conversationService.addMessage(request.getUserId(), assistantMsg);
                    })
                    .doOnError(error -> {
                        log.error("Error in chat streaming: {}", error.getMessage());
                    });

        } catch (Exception e) {
            log.error("Error processing chat request: {}", e.getMessage());
            return Flux.error(new RuntimeException("Error processing chat request: " + e.getMessage()));
        }
    }

    /**
     * Get conversation history for a user
     * GET /api/conversations/{userId}
     */
    @GetMapping("/conversations/{userId}")
    public ResponseEntity<ConversationResponse> getConversations(@PathVariable String userId) {
        List<ConversationMessage> conversations = conversationService.getUserConversations(userId);

        if (conversations.isEmpty()) {
            return ResponseEntity.ok(new ConversationResponse(
                    userId,
                    new ArrayList<>(),
                    0,
                    "No conversations found for this user"));
        }

        return ResponseEntity.ok(new ConversationResponse(
                userId,
                conversations,
                conversations.size(),
                null));
    }
}
