package com.navigator.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navigator.model.ConversationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Service for managing user conversation history.
 * Stores conversations in a JSON file for persistence.
 */
@Slf4j
@Service
public class ConversationService {

    @Value("${storage.conversations-file:/tmp/conversations.json}")
    private String conversationsFile;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Load all conversations from file
     */
    public Map<String, List<ConversationMessage>> loadConversations() {
        try {
            File file = new File(conversationsFile);
            if (file.exists()) {
                return objectMapper.readValue(file,
                        new TypeReference<Map<String, List<ConversationMessage>>>() {
                        });
            }
        } catch (IOException e) {
            log.error("Error loading conversations: {}", e.getMessage());
        }
        return new HashMap<>();
    }

    /**
     * Save all conversations to file
     */
    public void saveConversations(Map<String, List<ConversationMessage>> conversations) {
        try {
            File file = new File(conversationsFile);
            file.getParentFile().mkdirs();
            objectMapper.writeValue(file, conversations);
        } catch (IOException e) {
            log.error("Error saving conversations: {}", e.getMessage());
        }
    }

    /**
     * Get conversation history for a specific user
     */
    public List<ConversationMessage> getUserConversations(String userId) {
        Map<String, List<ConversationMessage>> conversations = loadConversations();
        return conversations.getOrDefault(userId, new ArrayList<>());
    }

    /**
     * Add a message to user's conversation history
     */
    public void addMessage(String userId, ConversationMessage message) {
        Map<String, List<ConversationMessage>> conversations = loadConversations();
        conversations.computeIfAbsent(userId, k -> new ArrayList<>()).add(message);
        saveConversations(conversations);
    }

    /**
     * Get last N messages for a user
     */
    public List<ConversationMessage> getLastNMessages(String userId, int n) {
        List<ConversationMessage> allMessages = getUserConversations(userId);
        int size = allMessages.size();
        if (size <= n) {
            return allMessages;
        }
        return allMessages.subList(size - n, size);
    }

    /**
     * Clear conversation history for a user
     */
    public void clearUserConversations(String userId) {
        Map<String, List<ConversationMessage>> conversations = loadConversations();
        conversations.remove(userId);
        saveConversations(conversations);
    }
}
