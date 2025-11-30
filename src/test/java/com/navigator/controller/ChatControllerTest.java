package com.navigator.controller;

import com.navigator.model.ConversationMessage;
import com.navigator.model.request.ChatRequest;
import com.navigator.service.ConversationService;
import com.navigator.service.OpenAIService;
import dev.langchain4j.data.message.ChatMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(ChatController.class)
public class ChatControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OpenAIService openAIService;

    @MockBean
    private ConversationService conversationService;

    @Test
    public void testChatEndpoint() {
        // Mock dependencies
        when(conversationService.getLastNMessages(anyString(), any(Integer.class)))
                .thenReturn(new ArrayList<>());

        when(openAIService.streamChatCompletion(any(List.class), anyString()))
                .thenReturn(Flux.just("Hello", " ", "World"));

        // Create request
        ChatRequest request = new ChatRequest();
        request.setUserMessage("Hi");
        request.setDeveloperMessage("System prompt");
        request.setUserId("user123");
        request.setApiKey("sk-test");

        // Perform request
        webTestClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Hello World");
    }

    @Test
    public void testGetConversations() {
        // Mock dependencies
        List<ConversationMessage> messages = new ArrayList<>();
        messages.add(new ConversationMessage("user", "Hi", Instant.now().toString()));
        when(conversationService.getUserConversations("user123"))
                .thenReturn(messages);

        // Perform request
        webTestClient.get()
                .uri("/api/conversations/user123")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userId").isEqualTo("user123")
                .jsonPath("$.conversations[0].content").isEqualTo("Hi");
    }
}
