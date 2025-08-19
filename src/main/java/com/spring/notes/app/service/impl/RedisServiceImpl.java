package com.spring.notes.app.service.impl;

import com.spring.notes.app.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Set key: {} with value: {}", key, value);
        } catch (Exception e) {
            log.error("Error setting key: {} with value: {}", key, value, e);
            throw e;
        }
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("Set key: {} with value: {} and TTL: {} {}", key, value, timeout, unit);
        } catch (Exception e) {
            log.error("Error setting key: {} with value: {} and TTL: {} {}", key, value, timeout, unit, e);
            throw e;
        }
    }

    @Override
    public Optional<Object> get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("Get key: {} -> value: {}", key, value);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            log.error("Error getting key: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && clazz.isInstance(value)) {
                log.debug("Get key: {} -> value: {} with type: {}", key, value, clazz.getSimpleName());
                return Optional.of((T) value);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting key: {} with type: {}", key, clazz.getSimpleName(), e);
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            log.debug("Delete key: {} -> success: {}", key, deleted);
        } catch (Exception e) {
            log.error("Error deleting key: {}", key, e);
            throw e;
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            log.debug("Exists key: {} -> {}", key, exists);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Error checking existence of key: {}", key, e);
            return false;
        }
    }

    @Override
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            Boolean expired = redisTemplate.expire(key, timeout, unit);
            log.debug("Set expiration for key: {} with TTL: {} {} -> success: {}", key, timeout, unit, expired);
            return Boolean.TRUE.equals(expired);
        } catch (Exception e) {
            log.error("Error setting expiration for key: {} with TTL: {} {}", key, timeout, unit, e);
            return false;
        }
    }

    @Override
    public Long getTtl(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key);
            log.debug("Get TTL for key: {} -> {}", key, ttl);
            return ttl;
        } catch (Exception e) {
            log.error("Error getting TTL for key: {}", key, e);
            return null;
        }
    }

    @Override
    public void clearAll() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
            log.info("Cleared all Redis keys");
        } catch (Exception e) {
            log.error("Error clearing all Redis keys", e);
            throw e;
        }
    }
}
