package com.example.oidc.storage;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class RedisClient {

    private final StringRedisTemplate redisTemplate;

    public RedisClient(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setValue(String key, String value, long expiration, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, expiration, timeUnit);
    }

    public void setValue(String key, String value) {
        long defaultExpiration = 300; // Default expiration time in seconds
        setValue(key, value, defaultExpiration, TimeUnit.SECONDS);
    }

    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public <T> void setObject(String key, T value, long expiration, TimeUnit timeUnit) {
        try {
            String json = objectMapper.writeValueAsString(value);
            setValue(key, json, expiration, timeUnit);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object", e);
        }
    }

    // Added overloaded setObject method without expiration parameters
    public <T> void setObject(String key, T value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            setValue(key, json); // Use default expiration
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object", e);
        }
    }

    public <T> T getObject(String key, Class<T> valueType) {
        String json = getValue(key);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize object", e);
        }
    }
}