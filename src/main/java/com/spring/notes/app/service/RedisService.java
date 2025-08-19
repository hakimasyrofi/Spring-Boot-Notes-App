package com.spring.notes.app.service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface RedisService {
    
    /**
     * Set a key-value pair in Redis
     */
    void set(String key, Object value);
    
    /**
     * Set a key-value pair in Redis with expiration
     */
    void set(String key, Object value, long timeout, TimeUnit unit);
    
    /**
     * Get a value by key
     */
    Optional<Object> get(String key);
    
    /**
     * Get a value by key with type casting
     */
    <T> Optional<T> get(String key, Class<T> clazz);
    
    /**
     * Delete a key
     */
    void delete(String key);
    
    /**
     * Check if a key exists
     */
    boolean exists(String key);
    
    /**
     * Set expiration for a key
     */
    boolean expire(String key, long timeout, TimeUnit unit);
    
    /**
     * Get time to live for a key
     */
    Long getTtl(String key);
    
    /**
     * Clear all keys (use with caution)
     */
    void clearAll();
}
