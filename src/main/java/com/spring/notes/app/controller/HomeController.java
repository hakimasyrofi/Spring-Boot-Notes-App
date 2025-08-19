package com.spring.notes.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "Home", description = "Welcome and information endpoints")
public class HomeController {
    
    @GetMapping("/")
    @Operation(summary = "Welcome message", description = "Get welcome message and API information")
    public ResponseEntity<Map<String, String>> home() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to Notes API");
        response.put("version", "1.0.0");
        response.put("description", "A professional notes/task management API built with Spring Boot");
        response.put("documentation", "/swagger-ui.html");
        response.put("h2-console", "/h2-console");
        response.put("auth-register", "/api/v1/auth/register");
        response.put("auth-login", "/api/v1/auth/login");
        response.put("notes-api", "/api/v1/notes");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the API is running")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", java.time.Instant.now().toString());
        return ResponseEntity.ok(response);
    }
}
