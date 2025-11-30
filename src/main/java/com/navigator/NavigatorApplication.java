package com.navigator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import lombok.extern.slf4j.Slf4j;

/**
 * Main Spring Boot application class for Navigator Educational AI System.
 * 
 * This application provides:
 * - Foundational Skill Diagnostician Agent
 * - RAG (Retrieval Augmented Generation) with Qdrant
 * - Quiz system with diagnostic feedback
 * - Progress tracking for students
 */
@Slf4j
@SpringBootApplication
public class NavigatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(NavigatorApplication.class, args);
    }

    /**
     * Startup event handler - equivalent to Python's @app.on_event("startup")
     * Initializes Qdrant and auto-loads PDFs if collection is empty
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("🚀 Navigator application started successfully");
        log.info("📚 Backend running on http://localhost:7860");
        log.info("💡 Use /api/health to check system status");
    }
}
