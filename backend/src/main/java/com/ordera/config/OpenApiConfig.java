package com.ordera.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ordera Multi-Tenant Loyalty & Ordering API")
                        .version("1.0.0")
                        .description("REST API documentation for Ordera platform (Java Spring Boot 3.x Edition)")
                        .contact(new Contact().name("Ordera Architecture Team").email("dev@ordera.no"))
                        .license(new License().name("Proprietary").url("https://ordera.no")));
    }
}

