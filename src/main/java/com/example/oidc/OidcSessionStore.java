package com.example.oidc;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class OidcSessionStore {
    private static final String MOBILEID_SESSION_PREFIX = "mobileid:session:";
    // Store MobileId session in Redis
    public void storeMobileIdSession(String sessionId, App.MobileIdSession session) {
        try {
            String json = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(MOBILEID_SESSION_PREFIX + sessionId, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize MobileIdSession", e);
        }
    }

    // Retrieve MobileId session from Redis
    public App.MobileIdSession getMobileIdSession(String sessionId) {
        String json = redisTemplate.opsForValue().get(MOBILEID_SESSION_PREFIX + sessionId);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, App.MobileIdSession.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize MobileIdSession", e);
        }
    }
    private static final String CODE_PREFIX = "oidc:code:";
    private static final String TOKEN_PREFIX = "oidc:token:";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public OidcSessionStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void storeCode(String code, UserInfo user) {
        try {
            String json = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(CODE_PREFIX + code, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize UserInfo", e);
        }
    }

    public UserInfo getUserByCode(String code) {
        String json = redisTemplate.opsForValue().get(CODE_PREFIX + code);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, UserInfo.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize UserInfo", e);
        }
    }

    public void storeToken(String token, UserInfo user) {
        try {
            String json = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(TOKEN_PREFIX + token, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize UserInfo", e);
        }
    }

    public UserInfo getUserByToken(String token) {
        String json = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, UserInfo.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize UserInfo", e);
        }
    }
    // ...existing code...
    public static class UserInfo {
    private String sub;
    private String name;
    private String email;
    private String country;
    private String phoneNumber;
    private String nonce;

        public UserInfo() {}
        public UserInfo(String sub, String name, String email, String country, String phoneNumber, String nonce) {
            this.sub = sub;
            this.name = name;
            this.email = email;
            this.country = country;
            this.phoneNumber = phoneNumber;
            this.nonce = nonce;
        }
        public String getSub() { return sub; }
        public void setSub(String sub) { this.sub = sub; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }
    }
}
