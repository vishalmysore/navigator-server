package com.navigator.model.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response model for evaluation endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationResponse {
    private boolean success;
    private EvaluationData data;
    private Map<String, String> meta;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EvaluationData {
        private double score;
        private String evaluation;
        private String nextStep;
        private String feedback;
    }
}
