package com.navigator.controller;

import com.navigator.config.OpenAIConfig;
import com.navigator.model.response.IntegrationTestResponse;
import com.navigator.service.OpenAIService;
import dev.langchain4j.data.message.UserMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for testing API integrations
 */
@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Integration Tests", description = "Test API integrations with external services")
public class IntegrationTestController {

    private final OpenAIService openAIService;
    private final OpenAIConfig openAIConfig;
    
    @Value("${openai.api-key}")
    private String openaiApiKey;
    
    @Value("${cohere.api-key:}")
    private String cohereApiKey;
    
    @Value("${tavily.api-key:}")
    private String tavilyApiKey;

    public IntegrationTestController(OpenAIService openAIService, OpenAIConfig openAIConfig) {
        this.openAIService = openAIService;
        this.openAIConfig = openAIConfig;
    }

    @GetMapping("/test-integrations")
    @Operation(summary = "Test API integrations", 
               description = "Tests connections to OpenAI, Cohere, and Tavily APIs")
    public ResponseEntity<Map<String, Map<String, String>>> testIntegrations() {
        Map<String, Map<String, String>> results = new HashMap<>();
        
        // Initialize results for each service
        results.put("openai", createStatusMap("not_tested", null, null));
        results.put("cohere", createStatusMap("not_tested", null, null));
        results.put("tavily", createStatusMap("not_tested", null, null));
        
        // Test OpenAI
        testOpenAI(results);
        
        // Test Cohere
        testCohere(results);
        
        // Test Tavily
        testTavily(results);
        
        return ResponseEntity.ok(results);
    }
    
    private void testOpenAI(Map<String, Map<String, String>> results) {
        try {
            log.info("Testing OpenAI integration...");
            String response = openAIService.chatCompletion(
                List.of(UserMessage.from("Say 'Hello'")),
                openaiApiKey
            );
            results.put("openai", createStatusMap("success", null, response));
            log.info("✅ OpenAI test successful");
        } catch (Exception e) {
            log.error("❌ OpenAI test failed: {}", e.getMessage());
            results.put("openai", createStatusMap("failed", e.getMessage(), null));
        }
    }
    
    private void testCohere(Map<String, Map<String, String>> results) {
        if (cohereApiKey == null || cohereApiKey.isEmpty()) {
            results.put("cohere", createStatusMap("skipped", "API key not configured", null));
            log.info("⚠️  Cohere test skipped - API key not configured");
            return;
        }
        
        try {
            log.info("Testing Cohere integration...");
            WebClient webClient = WebClient.create("https://api.cohere.ai");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "rerank-english-v3.0");
            requestBody.put("query", "test query");
            requestBody.put("documents", List.of("test document 1", "test document 2"));
            requestBody.put("top_n", 1);
            
            Map<String, Object> response = webClient.post()
                .uri("/v1/rerank")
                .header("Authorization", "Bearer " + cohereApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            List<?> rerankResults = (List<?>) response.get("results");
            String message = "Reranked " + (rerankResults != null ? rerankResults.size() : 0) + " documents";
            results.put("cohere", createStatusMap("success", null, message));
            log.info("✅ Cohere test successful");
        } catch (Exception e) {
            log.error("❌ Cohere test failed: {}", e.getMessage());
            results.put("cohere", createStatusMap("failed", e.getMessage(), null));
        }
    }
    
    private void testTavily(Map<String, Map<String, String>> results) {
        if (tavilyApiKey == null || tavilyApiKey.isEmpty()) {
            results.put("tavily", createStatusMap("skipped", "API key not configured", null));
            log.info("⚠️  Tavily test skipped - API key not configured");
            return;
        }
        
        try {
            log.info("Testing Tavily integration...");
            WebClient webClient = WebClient.create("https://api.tavily.com");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("api_key", tavilyApiKey);
            requestBody.put("query", "test search");
            requestBody.put("max_results", 1);
            
            Map<String, Object> response = webClient.post()
                .uri("/search")
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            List<?> searchResults = (List<?>) response.get("results");
            String message = "Found " + (searchResults != null ? searchResults.size() : 0) + " results";
            results.put("tavily", createStatusMap("success", null, message));
            log.info("✅ Tavily test successful");
        } catch (Exception e) {
            log.error("❌ Tavily test failed: {}", e.getMessage());
            results.put("tavily", createStatusMap("failed", e.getMessage(), null));
        }
    }
    
    private Map<String, String> createStatusMap(String status, String error, String response) {
        Map<String, String> map = new HashMap<>();
        map.put("status", status);
        if (error != null) {
            map.put("error", error);
        }
        if (response != null) {
            map.put("response", response);
        }
        return map;
    }
}
