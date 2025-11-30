package com.navigator.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents a document with text, embedding, and metadata.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Document {
    private String text;
    private List<Float> embedding;
    private Map<String, Object> metadata;

    public Document(String text, Map<String, Object> metadata) {
        this.text = text;
        this.metadata = metadata;
    }
}
