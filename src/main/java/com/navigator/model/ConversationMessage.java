package com.navigator.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Represents a single message in a conversation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationMessage {
    private String role; // "user", "assistant", "system"
    private String content;
    private String timestamp;
}
