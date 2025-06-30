package com.banking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger configuration
 */
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking Transaction Management System API")
                        .description("A simple banking transaction management system, supporting normal operations for transactions")
                        .version("1.0.0")
                        .contact(new Contact().name("David")))
                .servers(List.of(new Server().url("http://localhost:8080/banking").description("Development Environment")
                ));
    }
} 