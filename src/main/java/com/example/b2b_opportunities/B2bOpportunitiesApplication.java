package com.example.b2b_opportunities;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(
        info = @Info(
                title = "B2B",
                version = "1.0",
                description = "Backend API (26.11.24) - Prometheus"
        ))
public class B2bOpportunitiesApplication {

    public static void main(String[] args) {
        SpringApplication.run(B2bOpportunitiesApplication.class, args);
    }
}
