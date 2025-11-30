package com.navigator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8000}")
    private int serverPort;

    @Bean
    public OpenAPI navigatorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Navigator Backend API")
                        .description("Educational AI Diagnostician - Java Backend API Documentation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Navigator Team")
                                .email("support@navigator.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")
                ));
    }
}
