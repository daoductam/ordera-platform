package com.ordera.modules.health;

import com.ordera.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Kiểm tra tình trạng hoạt động của hệ thống")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> status = Map.of(
                "status", "UP",
                "service", "ordera-api",
                "version", "1.0.0",
                "javaVersion", System.getProperty("java.version"),
                "framework", "Spring Boot 3.3.5"
        );
        return ResponseEntity.ok(ApiResponse.ok("Ordera API is running healthy", status));
    }
}

