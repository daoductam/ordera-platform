package com.ordera.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String TENANT_HEADER_SCHEME = "TenantIdHeader";
    public static final String BEARER_AUTH_SCHEME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ordera Multi-Tenant Loyalty & Ordering API")
                        .version("1.0.0")
                        .description("REST API documentation for Ordera platform (Java Spring Boot 3.x Edition)")
                        .contact(new Contact().name("Ordera Architecture Team").email("dev@ordera.no"))
                        .license(new License().name("Proprietary").url("https://ordera.no")))
                .components(new Components()
                        .addSecuritySchemes(TENANT_HEADER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Tenant-ID")
                                .description("Mã UUID định danh thương hiệu (Multi-Tenant Header). Mẫu Linn Sushi: a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"))
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Token xác thực hội viên / nhân viên")))
                .addSecurityItem(new SecurityRequirement().addList(TENANT_HEADER_SCHEME));
    }

    @Bean
    public OperationCustomizer addGlobalTenantHeader() {
        return (operation, handlerMethod) -> {
            boolean hasTenantHeader = operation.getParameters() != null && operation.getParameters().stream()
                    .anyMatch(p -> "X-Tenant-ID".equalsIgnoreCase(p.getName()));

            if (!hasTenantHeader) {
                operation.addParametersItem(
                        new HeaderParameter()
                                .name("X-Tenant-ID")
                                .description("Mã UUID định danh thương hiệu (Mẫu Linn Sushi: a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11)")
                                .required(false)
                                .schema(new StringSchema().example("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"))
                );
            }
            return operation;
        };
    }
}

