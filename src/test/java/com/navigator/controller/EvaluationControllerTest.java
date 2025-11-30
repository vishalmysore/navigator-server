package com.navigator.controller;

import com.navigator.agent.AgentState;
import com.navigator.agent.DiagnosticianAgent;
import com.navigator.model.request.EvaluateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(EvaluationController.class)
public class EvaluationControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DiagnosticianAgent diagnosticianAgent;

    @Test
    public void testEvaluateEndpoint() {
        // Mock dependencies
        AgentState mockState = new AgentState();
        mockState.setScore(0.9);
        mockState.setEvaluation("Correct");
        mockState.setNextStep("Next topic");
        mockState.setFeedback("Good job");

        when(diagnosticianAgent.execute(any(AgentState.class)))
                .thenReturn(mockState);

        // Create request
        EvaluateRequest request = new EvaluateRequest();
        request.setQuestion("What is 2+2?");
        request.setAnswer("4");
        request.setApiKey("sk-test");

        // Perform request
        webTestClient.post()
                .uri("/api/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.score").isEqualTo(0.9)
                .jsonPath("$.data.evaluation").isEqualTo("Correct")
                .jsonPath("$.data.feedback").isEqualTo("Good job");
    }
}
