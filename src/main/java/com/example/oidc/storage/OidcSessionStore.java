package com.example.oidc.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.oidc.dto.MobileIdSession;
import com.example.oidc.dto.SmartIdSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OidcSessionStore {
    private static final String MOBILEID_SESSION_PREFIX = "mobileid:session:";
    private static final String CODE_PREFIX = "oidc:code:";
    private static final String TOKEN_PREFIX = "oidc:token:";
    private static final String SMARTID_SESSION_PREFIX = "smartid:session:";

    private static final Logger log = LoggerFactory.getLogger(OidcSessionStore.class);

    private final RedisClient redisClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public OidcSessionStore(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public void storeMobileIdSession(String sessionId, MobileIdSession session) {
        try {
            redisClient.setObject(MOBILEID_SESSION_PREFIX + sessionId, session);
        } catch (Exception e) {
            log.error("Failed to store MobileId session {}: {}", sessionId, e.getMessage());
        }
    }

    public MobileIdSession getMobileIdSession(String sessionId) {
        try {
            return redisClient.getObject(MOBILEID_SESSION_PREFIX + sessionId, MobileIdSession.class);
        } catch (Exception e) {
            log.error("Failed to get MobileId session {}: {}", sessionId, e.getMessage());
            return null;
        }
    }

    public void storeSmartIdSession(String sessionId, SmartIdSession session) {
        try {
            redisClient.setObject(SMARTID_SESSION_PREFIX + sessionId, session);
        } catch (Exception e) {
            log.error("Failed to store SmartId session {}: {}", sessionId, e.getMessage());
        }
    }

    public SmartIdSession getSmartIdSession(String sessionId) {
        try {
            return redisClient.getObject(SMARTID_SESSION_PREFIX + sessionId, SmartIdSession.class);
        } catch (Exception e) {
            log.error("Failed to get SmartId session {}: {}", sessionId, e.getMessage());
            return null;
        }
    }

    public void storeCode(String code, UserInfo user) {
        try {
            redisClient.setObject(CODE_PREFIX + code, user);
        } catch (Exception e) {
            log.error("Failed to store code {}: {}", code, e.getMessage());
        }
    }

    public void storeToken(String token, UserInfo user) {
        try {
            redisClient.setObject(TOKEN_PREFIX + token, user);
        } catch (Exception e) {
            log.error("Failed to store token {}: {}", token, e.getMessage());
        }
    }

    public UserInfo getUserByCode(String code) {
        String json;
        try {
            json = redisClient.getValue(CODE_PREFIX + code);
        } catch (Exception e) {
            log.error("Failed to fetch code {}: {}", code, e.getMessage());
            return null;
        }
        if (json == null)
            return null;
        try {
            return objectMapper.readValue(json, UserInfo.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize UserInfo for code {}: {}", code, e.getMessage());
            return null;
        }
    }

    public UserInfo getUserByToken(String token) {
        String json;
        try {
            json = redisClient.getValue(TOKEN_PREFIX + token);
        } catch (Exception e) {
            log.error("Failed to fetch token {}: {}", token, e.getMessage());
            return null;
        }
        if (json == null)
            return null;
        try {
            return objectMapper.readValue(json, UserInfo.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize UserInfo for token {}: {}", token, e.getMessage());
            return null;
        }
    }
}