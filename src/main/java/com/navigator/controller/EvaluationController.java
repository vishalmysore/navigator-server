package com.navigator.controller;

import com.navigator.agent.AgentState;
import com.navigator.agent.DiagnosticianAgent;
import com.navigator.config.OpenAIConfig;
import com.navigator.model.request.EvaluateRequest;
import com.navigator.model.response.EvaluationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * REST controller for evaluation endpoints.
 * Provides diagnostic evaluation of student answers using the agent system.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class EvaluationController {

    private final DiagnosticianAgent diagnosticianAgent;
    private final OpenAIConfig openAIConfig;

    public EvaluationController(DiagnosticianAgent diagnosticianAgent, OpenAIConfig openAIConfig) {
        this.diagnosticianAgent = diagnosticianAgent;
        this.openAIConfig = openAIConfig;
    }

    /**
     * Evaluate student answer
     * POST /api/evaluate
     */
    @PostMapping("/evaluate")
    public ResponseEntity<EvaluationResponse> evaluate(@Valid @RequestBody EvaluateRequest request) {
        try {
            log.info("Evaluating answer for question: {}", request.getQuestion());

            // Create agent state
            AgentState state = new AgentState();
            state.setQuestion(request.getQuestion());
            state.setAnswer(request.getAnswer());
            state.setContext(request.getContext());
            // Use provided API key or fall back to configured one
            String apiKey = (request.getApiKey() != null && !request.getApiKey().isBlank()) 
                    ? request.getApiKey() 
                    : openAIConfig.getApiKey();
            state.setApiKey(apiKey);

            // Execute diagnostic agent
            AgentState result = diagnosticianAgent.execute(state);

            // Build response
            EvaluationResponse.EvaluationData data = new EvaluationResponse.EvaluationData(
                    result.getScore(),
                    result.getEvaluation(),
                    result.getNextStep(),
                    result.getFeedback());

            Map<String, String> meta = Map.of(
                    "model", "gpt-4o-mini",
                    "agent", "diagnostician",
                    "version", "v2");

            EvaluationResponse response = new EvaluationResponse(true, data, meta);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in evaluation: {}", e.getMessage());

            EvaluationResponse.EvaluationData errorData = new EvaluationResponse.EvaluationData(
                    0.0,
                    "Error during evaluation",
                    "Please try again",
                    "An error occurred while evaluating your answer");

            Map<String, String> meta = Map.of(
                    "model", "gpt-4o-mini",
                    "agent", "diagnostician",
                    "version", "v2");

            EvaluationResponse response = new EvaluationResponse(false, errorData, meta);

            return ResponseEntity.status(500).body(response);
        }
    }
}
