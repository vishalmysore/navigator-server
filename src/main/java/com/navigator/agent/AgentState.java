package com.navigator.agent;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * State model for the diagnostic agent.
 * Represents the current state of the diagnostic workflow.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentState {
    private String question;
    private String answer;
    private String context;
    private String apiKey;
    private String agentResponse;
    private double score;
    private String evaluation;
    private String nextStep;
    private String feedback;
}
