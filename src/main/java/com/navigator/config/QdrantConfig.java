package com.navigator.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Qdrant vector database configuration.
 */
@Slf4j
@Configuration
public class QdrantConfig {

    @Value("${qdrant.url:./qdrant_local}")
    private String qdrantUrl;

    @Value("${qdrant.collection-name:science_curriculum_g3_g6}")
    private String collectionName;

    @Value("${qdrant.vector-size:1536}")
    private int vectorSize;

    @Bean
    public QdrantClient qdrantClient() {
        try {
            // Check if URL is a local path or remote URL
            if (qdrantUrl.startsWith("http://") || qdrantUrl.startsWith("https://")) {
                // Remote Qdrant instance
                String host = qdrantUrl.replace("http://", "").replace("https://", "");
                int port = 6334; // Default gRPC port

                if (host.contains(":")) {
                    String[] parts = host.split(":");
                    host = parts[0];
                    port = Integer.parseInt(parts[1]);
                }

                log.info("🔗 Connecting to Qdrant at {}:{}", host, port);
                return new QdrantClient(QdrantGrpcClient.newBuilder(host, port, false).build());
            } else {
                // Local file-based Qdrant (for development)
                log.info("📁 Using local Qdrant storage at: {}", qdrantUrl);
                // For local development, we'll use the in-memory RAG system
                // In production, you would set up a proper Qdrant instance
                return null; // Will use RAGService instead
            }
        } catch (Exception e) {
            log.warn("⚠️  Could not connect to Qdrant: {}. Using in-memory RAG system.", e.getMessage());
            return null;
        }
    }

    public String getCollectionName() {
        return collectionName;
    }

    public int getVectorSize() {
        return vectorSize;
    }
}
