package com.navigator.model.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import com.navigator.model.ConversationMessage;

/**
 * Response model for conversation history endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResponse {
    private String userId;
    private List<ConversationMessage> conversations;
    private int totalMessages;
    private String message;
}
