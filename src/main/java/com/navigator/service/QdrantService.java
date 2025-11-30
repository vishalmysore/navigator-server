package com.navigator.service;

import com.navigator.config.QdrantConfig;
import com.navigator.model.Document;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.ValueFactory;
import io.qdrant.client.grpc.Collections.CollectionInfo;
import io.qdrant.client.grpc.Collections.CreateCollection;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Collections.VectorsConfig;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.Vector;
import io.qdrant.client.grpc.Points.Vectors;
import io.qdrant.client.grpc.Points.WithPayloadSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for Qdrant vector database operations.
 * Handles collection management, vector upsert, and similarity search.
 */
@Slf4j
@Service
public class QdrantService {

    private final QdrantClient qdrantClient;
    private final QdrantConfig qdrantConfig;
    private final OpenAIService openAIService;

    public QdrantService(@Autowired(required = false) QdrantClient qdrantClient, 
                         QdrantConfig qdrantConfig, 
                         OpenAIService openAIService) {
        this.qdrantClient = qdrantClient;
        this.qdrantConfig = qdrantConfig;
        this.openAIService = openAIService;
    }

    /**
     * Check if Qdrant is available
     */
    public boolean isAvailable() {
        return qdrantClient != null;
    }

    /**
     * Initialize collection if it doesn't exist
     */
    public void initializeCollection() {
        if (!isAvailable()) {
            log.info("Qdrant not available, skipping collection initialization");
            return;
        }

        try {
            String collectionName = qdrantConfig.getCollectionName();

            // Check if collection exists
            boolean exists = collectionExists(collectionName);

            if (!exists) {
                log.info("Creating Qdrant collection: {}", collectionName);

                VectorParams vectorParams = VectorParams.newBuilder()
                        .setSize(qdrantConfig.getVectorSize())
                        .setDistance(Distance.Cosine)
                        .build();

                CreateCollection createCollection = CreateCollection.newBuilder()
                        .setCollectionName(collectionName)
                        .setVectorsConfig(VectorsConfig.newBuilder()
                                .setParams(vectorParams)
                                .build())
                        .build();

                qdrantClient.createCollectionAsync(createCollection).get();
                log.info("✅ Collection created: {}", collectionName);
            } else {
                log.info("✅ Collection already exists: {}", collectionName);
            }
        } catch (Exception e) {
            log.error("Error initializing Qdrant collection: {}", e.getMessage());
        }
    }

    /**
     * Check if collection exists
     */
    public boolean collectionExists(String collectionName) {
        if (!isAvailable()) {
            return false;
        }

        try {
            qdrantClient.getCollectionInfoAsync(collectionName).get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Upsert documents into Qdrant
     */
    public void upsertDocuments(List<Document> documents, String apiKey) {
        if (!isAvailable()) {
            log.warn("Qdrant not available, cannot upsert documents");
            return;
        }

        try {
            String collectionName = qdrantConfig.getCollectionName();
            List<PointStruct> points = new ArrayList<>();

            for (int i = 0; i < documents.size(); i++) {
                Document doc = documents.get(i);

                // Generate embedding if not present
                List<Float> embedding = doc.getEmbedding();
                if (embedding == null || embedding.isEmpty()) {
                    embedding = openAIService.createEmbedding(doc.getText(), apiKey);
                    doc.setEmbedding(embedding);
                }

                // Create point
                PointStruct point = PointStruct.newBuilder()
                        .setId(PointId.newBuilder().setUuid(UUID.randomUUID().toString()).build())
                        .setVectors(Vectors.newBuilder()
                                .setVector(Vector.newBuilder()
                                        .addAllData(embedding)
                                        .build())
                                .build())
                        .putAllPayload(convertMetadataToPayload(doc.getMetadata()))
                        .build();

                points.add(point);
            }

            // Upsert points
            qdrantClient.upsertAsync(collectionName, points).get();
            log.info("✅ Upserted {} documents to Qdrant", documents.size());
        } catch (Exception e) {
            log.error("Error upserting documents to Qdrant: {}", e.getMessage());
        }
    }

    /**
     * Search for similar vectors
     */
    public List<Document> searchTopK(String query, int k, String apiKey) {
        if (!isAvailable()) {
            log.warn("Qdrant not available, returning empty results");
            return new ArrayList<>();
        }

        try {
            // Generate query embedding
            List<Float> queryEmbedding = openAIService.createEmbedding(query, apiKey);

            // Search
            String collectionName = qdrantConfig.getCollectionName();
            SearchPoints searchPoints = SearchPoints.newBuilder()
                    .setCollectionName(collectionName)
                    .addAllVector(queryEmbedding)
                    .setLimit(k)
                    .setWithPayload(WithPayloadSelector.newBuilder().setEnable(true).build())
                    .build();

            List<ScoredPoint> results = qdrantClient.searchAsync(searchPoints).get();

            // Convert results to documents
            List<Document> documents = new ArrayList<>();
            for (ScoredPoint result : results) {
                Map<String, Value> payload = result.getPayloadMap();
                String text = payload.containsKey("text") ? payload.get("text").getStringValue() : "";

                Document doc = new Document();
                doc.setText(text);
                doc.setMetadata(convertPayloadToMetadata(payload));
                documents.add(doc);
            }

            return documents;
        } catch (Exception e) {
            log.error("Error searching Qdrant: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get collection info
     */
    public Map<String, Object> getCollectionInfo() {
        if (!isAvailable()) {
            return Map.of("available", false);
        }

        try {
            String collectionName = qdrantConfig.getCollectionName();
            CollectionInfo info = qdrantClient.getCollectionInfoAsync(collectionName).get();

            return Map.of(
                    "available", true,
                    "name", collectionName,
                    "vectorsCount", info.getVectorsCount(),
                    "pointsCount", info.getPointsCount());
        } catch (Exception e) {
            return Map.of("available", false, "error", e.getMessage());
        }
    }

    /**
     * Convert Qdrant payload to metadata
     */
    private Map<String, Object> convertPayloadToMetadata(Map<String, Value> payload) {
        Map<String, Object> metadata = new HashMap<>();

        for (Map.Entry<String, Value> entry : payload.entrySet()) {
            Value value = entry.getValue();

            if (value.hasStringValue()) {
                metadata.put(entry.getKey(), value.getStringValue());
            } else if (value.hasIntegerValue()) {
                metadata.put(entry.getKey(), value.getIntegerValue());
            } else if (value.hasDoubleValue()) {
                metadata.put(entry.getKey(), value.getDoubleValue());
            } else if (value.hasBoolValue()) {
                metadata.put(entry.getKey(), value.getBoolValue());
            }
        }

        return metadata;
    }

    /**
     * Convert metadata to Qdrant payload
     */
    private Map<String, Value> convertMetadataToPayload(Map<String, Object> metadata) {
        Map<String, Value> payload = new HashMap<>();

        if (metadata != null) {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                Object value = entry.getValue();

                if (value instanceof String) {
                    payload.put(entry.getKey(), ValueFactory.value((String) value));
                } else if (value instanceof Integer) {
                    payload.put(entry.getKey(), ValueFactory.value((Integer) value));
                } else if (value instanceof Long) {
                    payload.put(entry.getKey(), ValueFactory.value((Long) value));
                } else if (value instanceof Double) {
                    payload.put(entry.getKey(), ValueFactory.value((Double) value));
                } else if (value instanceof Float) {
                    payload.put(entry.getKey(), ValueFactory.value((Float) value));
                } else if (value instanceof Boolean) {
                    payload.put(entry.getKey(), ValueFactory.value((Boolean) value));
                } else {
                    payload.put(entry.getKey(), ValueFactory.value(value.toString()));
                }
            }
        }

        return payload;
    }
}
