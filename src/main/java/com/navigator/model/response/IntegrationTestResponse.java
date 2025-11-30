package com.navigator.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response for integration test endpoint
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationTestResponse {
    private Map<String, ServiceStatus> openai;
    private Map<String, ServiceStatus> cohere;
    private Map<String, ServiceStatus> tavily;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceStatus {
        private String status;
        private String error;
        private String response;
    }
}
