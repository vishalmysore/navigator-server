package com.navigator.model.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response model for search endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse {
    private String text;
    private double score;
    private Map<String, Object> metadata;
}
