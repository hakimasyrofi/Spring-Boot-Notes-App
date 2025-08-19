package com.spring.notes.app.controller;

import com.spring.notes.app.dto.response.ApiResponse;
import com.spring.notes.app.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Redis Management", description = "Redis cache management endpoints")
public class RedisController {

    private final RedisService redisService;

    @GetMapping("/health")
    @Operation(summary = "Check Redis health", description = "Check if Redis is accessible")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkRedisHealth() {
        try {
            // Test Redis connection by setting and getting a test key
            String testKey = "health:test";
            String testValue = "redis-health-check";
            
            redisService.set(testKey, testValue, 1, java.util.concurrent.TimeUnit.MINUTES);
            Optional<Object> result = redisService.get(testKey);
            
            Map<String, Object> healthInfo = new HashMap<>();
            healthInfo.put("status", "UP");
            healthInfo.put("message", "Redis is accessible");
            healthInfo.put("testKey", testKey);
            healthInfo.put("testValue", testValue);
            healthInfo.put("retrievedValue", result.orElse(null));
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Redis is healthy")
                    .data(healthInfo)
                    .build());
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            Map<String, Object> healthInfo = new HashMap<>();
            healthInfo.put("status", "DOWN");
            healthInfo.put("message", "Redis is not accessible");
            healthInfo.put("error", e.getMessage());
            
            return ResponseEntity.status(503).body(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Redis health check failed")
                    .data(healthInfo)
                    .build());
        }
    }

    @GetMapping("/key/{key}")
    @Operation(summary = "Get value by key", description = "Retrieve a value from Redis by key")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getValue(@PathVariable String key) {
        try {
            Optional<Object> value = redisService.get(key);
            if (value.isPresent()) {
                return ResponseEntity.ok(ApiResponse.<Object>builder()
                        .success(true)
                        .message("Value retrieved successfully")
                        .data(value.get())
                        .build());
            } else {
                return ResponseEntity.ok(ApiResponse.<Object>builder()
                        .success(false)
                        .message("Key not found")
                        .data(null)
                        .build());
            }
        } catch (Exception e) {
            log.error("Error retrieving key: {}", key, e);
            return ResponseEntity.status(500).body(ApiResponse.<Object>builder()
                    .success(false)
                    .message("Error retrieving key: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/key/{key}/exists")
    @Operation(summary = "Check if key exists", description = "Check if a key exists in Redis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> keyExists(@PathVariable String key) {
        try {
            boolean exists = redisService.exists(key);
            return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                    .success(true)
                    .message("Key existence checked")
                    .data(exists)
                    .build());
        } catch (Exception e) {
            log.error("Error checking key existence: {}", key, e);
            return ResponseEntity.status(500).body(ApiResponse.<Boolean>builder()
                    .success(false)
                    .message("Error checking key existence: " + e.getMessage())
                    .data(false)
                    .build());
        }
    }

    @GetMapping("/key/{key}/ttl")
    @Operation(summary = "Get key TTL", description = "Get time to live for a key")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getTtl(@PathVariable String key) {
        try {
            Long ttl = redisService.getTtl(key);
            return ResponseEntity.ok(ApiResponse.<Long>builder()
                    .success(true)
                    .message("TTL retrieved successfully")
                    .data(ttl)
                    .build());
        } catch (Exception e) {
            log.error("Error getting TTL for key: {}", key, e);
            return ResponseEntity.status(500).body(ApiResponse.<Long>builder()
                    .success(false)
                    .message("Error getting TTL: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @DeleteMapping("/key/{key}")
    @Operation(summary = "Delete key", description = "Delete a key from Redis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteKey(@PathVariable String key) {
        try {
            redisService.delete(key);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Key deleted successfully")
                    .data(null)
                    .build());
        } catch (Exception e) {
            log.error("Error deleting key: {}", key, e);
            return ResponseEntity.status(500).body(ApiResponse.<Void>builder()
                    .success(false)
                    .message("Error deleting key: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear all cache", description = "Clear all keys from Redis (use with caution)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> clearAllCache() {
        try {
            redisService.clearAll();
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("All cache cleared successfully")
                    .data(null)
                    .build());
        } catch (Exception e) {
            log.error("Error clearing all cache", e);
            return ResponseEntity.status(500).body(ApiResponse.<Void>builder()
                    .success(false)
                    .message("Error clearing cache: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }
}
