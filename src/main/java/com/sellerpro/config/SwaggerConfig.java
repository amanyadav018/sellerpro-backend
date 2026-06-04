package com.sellerpro.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI sellerProOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("SellerPro API")
                .description("SellerPro SaaS Backend — Multi-platform Indian Seller Analytics")
                .version("1.0.0")
                .contact(new Contact().name("SellerPro").email("dev@sellerpro.in")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
            .components(new Components()
                .addSecuritySchemes("Bearer Auth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
