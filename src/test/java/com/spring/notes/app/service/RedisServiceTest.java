package com.spring.notes.app.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RedisServiceTest {

    @Autowired
    private RedisService redisService;

    @Test
    void testSetAndGet() {
        // Given
        String key = "test:key";
        String value = "test-value";

        // When
        redisService.set(key, value);
        Optional<Object> result = redisService.get(key);

        // Then
        assertTrue(result.isPresent());
        assertEquals(value, result.get());

        // Cleanup
        redisService.delete(key);
    }

    @Test
    void testSetWithExpiration() throws InterruptedException {
        // Given
        String key = "test:expire";
        String value = "expire-value";

        // When
        redisService.set(key, value, 1, TimeUnit.SECONDS);
        Optional<Object> result1 = redisService.get(key);
        
        // Wait for expiration
        Thread.sleep(1100);
        Optional<Object> result2 = redisService.get(key);

        // Then
        assertTrue(result1.isPresent());
        assertEquals(value, result1.get());
        assertFalse(result2.isPresent());
    }

    @Test
    void testExists() {
        // Given
        String key = "test:exists";
        String value = "exists-value";

        // When
        boolean existsBefore = redisService.exists(key);
        redisService.set(key, value);
        boolean existsAfter = redisService.exists(key);

        // Then
        assertFalse(existsBefore);
        assertTrue(existsAfter);

        // Cleanup
        redisService.delete(key);
    }

    @Test
    void testDelete() {
        // Given
        String key = "test:delete";
        String value = "delete-value";

        // When
        redisService.set(key, value);
        boolean existsBefore = redisService.exists(key);
        redisService.delete(key);
        boolean existsAfter = redisService.exists(key);

        // Then
        assertTrue(existsBefore);
        assertFalse(existsAfter);
    }

    @Test
    void testGetWithType() {
        // Given
        String key = "test:type";
        Integer value = 42;

        // When
        redisService.set(key, value);
        Optional<Integer> result = redisService.get(key, Integer.class);

        // Then
        assertTrue(result.isPresent());
        assertEquals(value, result.get());

        // Cleanup
        redisService.delete(key);
    }
}
