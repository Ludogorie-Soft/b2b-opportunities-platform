package com.example.b2b_opportunities.Configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfiguration {

    @Bean
    public OpenAPI customizeOpenAPI() {
        final String bearerSchemeName = "bearerAuth";
        final String oauth2SchemeName = "oauth2";

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(bearerSchemeName))
                .addSecurityItem(new SecurityRequirement().addList(oauth2SchemeName))
                .components(new Components()
                        .addSecuritySchemes(bearerSchemeName, new SecurityScheme()
                                .name(bearerSchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSecuritySchemes(oauth2SchemeName, new SecurityScheme()
                                .name(oauth2SchemeName)
                                .type(SecurityScheme.Type.OAUTH2)
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl("https://accounts.google.com/o/oauth2/auth")
                                                .tokenUrl("https://oauth2.googleapis.com/token")
                                                .scopes(new Scopes()
                                                        .addString("profile", "Access your Google profile")
                                                        .addString("email", "Access your email address")
                                                )))));
    }
}