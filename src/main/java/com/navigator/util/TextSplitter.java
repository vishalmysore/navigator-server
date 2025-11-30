package com.navigator.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for splitting text into chunks.
 * Equivalent to Python's CharacterTextSplitter from LangChain.
 */
public class TextSplitter {

    private final int chunkSize;
    private final int chunkOverlap;
    private final String separator;

    public TextSplitter(int chunkSize, int chunkOverlap, String separator) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.separator = separator;
    }

    public TextSplitter(int chunkSize, int chunkOverlap) {
        this(chunkSize, chunkOverlap, "\n");
    }

    /**
     * Split text into chunks with overlap
     */
    public List<String> splitText(String text) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return chunks;
        }

        // Split by separator first
        String[] parts = text.split(separator);
        StringBuilder currentChunk = new StringBuilder();

        for (String part : parts) {
            // If adding this part would exceed chunk size, save current chunk
            if (currentChunk.length() + part.length() + separator.length() > chunkSize
                    && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());

                // Start new chunk with overlap
                String overlap = getOverlap(currentChunk.toString());
                currentChunk = new StringBuilder(overlap);
            }

            if (currentChunk.length() > 0) {
                currentChunk.append(separator);
            }
            currentChunk.append(part);
        }

        // Add the last chunk
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    /**
     * Get overlap text from the end of current chunk
     */
    private String getOverlap(String text) {
        if (text.length() <= chunkOverlap) {
            return text;
        }
        return text.substring(text.length() - chunkOverlap);
    }

    /**
     * Simple character-based splitting (fallback)
     */
    public static List<String> splitByCharacters(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return chunks;
        }

        for (int i = 0; i < text.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, text.length());
            chunks.add(text.substring(i, end));
        }

        return chunks;
    }
}
