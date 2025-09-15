package com.example.oidc.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.oidc.dto.MobileIdSession;
import com.example.oidc.dto.SmartIdSession;

@Component
public class OidcSessionStore {
    private static final String MOBILEID_SESSION_PREFIX = "mobileid:session:";
    private static final String CODE_PREFIX = "oidc:code:";
    private static final String TOKEN_PREFIX = "oidc:token:";
    private static final String SMARTID_SESSION_PREFIX = "smartid:session:";

    private final RedisClient redisClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public OidcSessionStore(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public void storeMobileIdSession(String sessionId, MobileIdSession session) {
        redisClient.setObject(MOBILEID_SESSION_PREFIX + sessionId, session);
    }

    public MobileIdSession getMobileIdSession(String sessionId) {
        return redisClient.getObject(MOBILEID_SESSION_PREFIX + sessionId, MobileIdSession.class);
    }

    public void storeSmartIdSession(String sessionId, SmartIdSession session) {
        redisClient.setObject(SMARTID_SESSION_PREFIX + sessionId, session);
    }

    public SmartIdSession getSmartIdSession(String sessionId) {
        return redisClient.getObject(SMARTID_SESSION_PREFIX + sessionId, SmartIdSession.class);
    }

    public void storeCode(String code, UserInfo user) {
        redisClient.setObject(CODE_PREFIX + code, user);
    }

    public void storeToken(String token, UserInfo user) {
        redisClient.setObject(TOKEN_PREFIX + token, user);
    }

    public UserInfo getUserByCode(String code) {
        String json = redisClient.getValue(CODE_PREFIX + code);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, UserInfo.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize UserInfo", e);
        }
    }

    public UserInfo getUserByToken(String token) {
        String json = redisClient.getValue(TOKEN_PREFIX + token);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, UserInfo.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize UserInfo", e);
        }
    }
}