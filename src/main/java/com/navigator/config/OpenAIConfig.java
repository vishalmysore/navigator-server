package com.navigator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

/**
 * OpenAI API configuration.
 * Manages API keys and model settings.
 */
@Configuration
@Getter
public class OpenAIConfig {

    @Value("${openai_api_key}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.embedding-model:text-embedding-3-small}")
    private String embeddingModel;

    @Value("${openai.max-tokens:500}")
    private int maxTokens;
}
