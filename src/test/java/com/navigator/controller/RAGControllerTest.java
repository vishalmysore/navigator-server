package com.navigator.controller;

import com.navigator.model.request.RAGChatRequest;
import com.navigator.service.ConversationService;
import com.navigator.service.RAGService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(RAGController.class)
public class RAGControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RAGService ragService;

    @MockBean
    private ConversationService conversationService;

    @Test
    public void testRagChatEndpoint() {
        // Mock dependencies
        when(ragService.query(anyString(), anyString()))
                .thenReturn("RAG Response");
        when(ragService.getDocumentCount()).thenReturn(5);

        // Create request
        RAGChatRequest request = new RAGChatRequest();
        request.setUserMessage("Question");
        request.setUserId("user123");
        request.setApiKey("sk-test");

        // Perform request
        webTestClient.post()
                .uri("/api/rag-chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("RAG Response")
                .jsonPath("$.documentsCount").isEqualTo(5);
    }

    @Test
    public void testRagStatusEndpoint() {
        // Mock dependencies
        when(ragService.getDocumentCount()).thenReturn(10);

        // Perform request
        webTestClient.get()
                .uri("/api/rag-status")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.hasIndex").isEqualTo(true)
                .jsonPath("$.documentsCount").isEqualTo(10)
                .jsonPath("$.status").isEqualTo("ready");
    }
}
