package com.navigator.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navigator.model.Document;
import com.navigator.service.OpenAIService;
import com.navigator.service.QdrantService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Diagnostic Agent for evaluating student answers.
 * Implements a multi-step workflow:
 * 1. Retrieve relevant context from knowledge base
 * 2. Analyze student answer against context
 * 3. Generate diagnostic feedback
 */
@Slf4j
@Component
public class DiagnosticianAgent {

    private final OpenAIService openAIService;
    private final QdrantService qdrantService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DiagnosticianAgent(OpenAIService openAIService, QdrantService qdrantService) {
        this.openAIService = openAIService;
        this.qdrantService = qdrantService;
    }

    /**
     * Execute the diagnostic agent workflow
     */
    public AgentState execute(AgentState state) {
        log.info("🤖 Executing diagnostic agent for question: {}", state.getQuestion());

        // Step 1: Retrieve context if not provided
        if (state.getContext() == null || state.getContext().isEmpty()) {
            state.setContext(retrieveContext(state.getQuestion(), state.getApiKey()));
        }

        // Step 2: Diagnose the answer
        diagnose(state);

        return state;
    }

    /**
     * Step 1: Retrieve relevant context from knowledge base
     */
    private String retrieveContext(String question, String apiKey) {
        log.info("📚 Retrieving context for question");

        try {
            if (qdrantService.isAvailable()) {
                // Use Qdrant for context retrieval
                List<Document> docs = qdrantService.searchTopK(question, 3, apiKey);
                return docs.stream()
                        .map(Document::getText)
                        .reduce("", (a, b) -> a + "\n\n" + b);
            } else {
                // No context available
                return "No additional context available.";
            }
        } catch (Exception e) {
            log.error("Error retrieving context: {}", e.getMessage());
            return "Error retrieving context.";
        }
    }

    /**
     * Step 2: Diagnose the student answer
     */
    private void diagnose(AgentState state) {
        log.info("🔍 Diagnosing student answer");

        try {
            // Create diagnostic prompt
            String systemPrompt = createDiagnosticPrompt(state);
            String userPrompt = String.format(
                    "Question: %s\n\nStudent Answer: %s\n\nProvide your evaluation in JSON format.",
                    state.getQuestion(),
                    state.getAnswer());

            // Get LLM evaluation
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(SystemMessage.from(systemPrompt));
            messages.add(UserMessage.from(userPrompt));

            String response = openAIService.chatCompletion(messages, state.getApiKey());

            // Parse JSON response
            parseAgentResponse(state, response);

        } catch (Exception e) {
            log.error("Error in diagnosis: {}", e.getMessage());
            setDefaultResponse(state);
        }
    }

    /**
     * Create the diagnostic prompt
     */
    private String createDiagnosticPrompt(AgentState state) {
        return String.format("""
                You are an expert educational diagnostician specializing in science education for grades 3-6.
                Your role is to evaluate student answers and provide constructive feedback.

                Context from curriculum:
                %s

                Evaluate the student's answer based on:
                1. Factual accuracy
                2. Conceptual understanding
                3. Completeness
                4. Use of scientific vocabulary

                Provide your response in the following JSON format:
                {
                    "score": <number between 0 and 1>,
                    "evaluation": "<brief evaluation of the answer>",
                    "next_step": "<suggested next learning step>",
                    "feedback": "<constructive feedback for the student>"
                }

                Be encouraging and constructive. Focus on what the student understands and what they need to work on.
                """, state.getContext());
    }

    /**
     * Parse the agent's JSON response
     */
    private void parseAgentResponse(AgentState state, String response) {
        try {
            // Try to extract JSON from response
            String jsonStr = response;
            if (response.contains("{")) {
                int start = response.indexOf("{");
                int end = response.lastIndexOf("}") + 1;
                jsonStr = response.substring(start, end);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(jsonStr, Map.class);

            state.setScore(((Number) result.getOrDefault("score", 0.0)).doubleValue());
            state.setEvaluation((String) result.getOrDefault("evaluation", ""));
            state.setNextStep((String) result.getOrDefault("next_step", ""));
            state.setFeedback((String) result.getOrDefault("feedback", ""));
            state.setAgentResponse(jsonStr);

        } catch (Exception e) {
            log.error("Error parsing agent response: {}", e.getMessage());
            setDefaultResponse(state);
        }
    }

    /**
     * Set default response if parsing fails
     */
    private void setDefaultResponse(AgentState state) {
        state.setScore(0.5);
        state.setEvaluation("Unable to fully evaluate the answer.");
        state.setNextStep("Review the question and try again.");
        state.setFeedback("Please provide more detail in your answer.");
        state.setAgentResponse("{\"score\": 0.5, \"evaluation\": \"Error in evaluation\"}");
    }
}
