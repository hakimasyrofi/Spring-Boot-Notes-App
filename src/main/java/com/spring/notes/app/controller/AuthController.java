package com.spring.notes.app.controller;

import com.spring.notes.app.dto.request.LoginRequest;
import com.spring.notes.app.dto.request.RegisterRequest;
import com.spring.notes.app.dto.response.ApiResponse;
import com.spring.notes.app.dto.response.AuthResponse;
import com.spring.notes.app.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization API")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());
        
        try {
            AuthResponse authResponse = authService.register(request);
            ApiResponse<AuthResponse> response = ApiResponse.success("User registered successfully", authResponse);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            ApiResponse<AuthResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());
        
        try {
            AuthResponse authResponse = authService.login(request);
            ApiResponse<AuthResponse> response = ApiResponse.success("Login successful", authResponse);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<AuthResponse> response = ApiResponse.error("Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
