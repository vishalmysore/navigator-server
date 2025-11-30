package com.navigator.util;

import java.util.List;

/**
 * Utility class for vector operations.
 * Includes cosine similarity and other embedding-related functions.
 */
public class EmbeddingUtil {

    /**
     * Calculate cosine similarity between two vectors
     */
    public static double cosineSimilarity(List<Float> vectorA, List<Float> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("Vectors must have the same dimension");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += vectorA.get(i) * vectorA.get(i);
            normB += vectorB.get(i) * vectorB.get(i);
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Calculate Euclidean distance between two vectors
     */
    public static double euclideanDistance(List<Float> vectorA, List<Float> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("Vectors must have the same dimension");
        }

        double sum = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            double diff = vectorA.get(i) - vectorB.get(i);
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    /**
     * Normalize a vector to unit length
     */
    public static List<Float> normalize(List<Float> vector) {
        double norm = 0.0;
        for (Float value : vector) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);

        if (norm == 0.0) {
            return vector;
        }

        java.util.List<Float> normalized = new java.util.ArrayList<>(vector.size());
        for (Float value : vector) {
            normalized.add((float) (value / norm));
        }

        return normalized;
    }
}
