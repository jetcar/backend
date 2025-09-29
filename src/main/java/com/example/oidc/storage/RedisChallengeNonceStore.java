package com.example.oidc.storage;

import eu.webeid.security.challenge.ChallengeNonce;
import eu.webeid.security.challenge.ChallengeNonceStore;

import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class RedisChallengeNonceStore implements ChallengeNonceStore {

    private static final String PREFIX = "webeid:nonce:";
    private static final long DEFAULT_EXPIRATION_SECONDS = 300;

    private final RedisClient redisClient;

    public RedisChallengeNonceStore(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public void put(ChallengeNonce challengeNonce) {
        if (challengeNonce == null)
            return;
        String key = PREFIX + challengeNonce.getBase64EncodedNonce();
        String value = (challengeNonce.getBase64EncodedNonce());
        redisClient.setValue(key, value, DEFAULT_EXPIRATION_SECONDS, TimeUnit.SECONDS);
    }

    // Custom method to get and remove by nonce value
    public ChallengeNonce getAndRemove(String nonce) {
        String key = PREFIX + nonce;
        String value = redisClient.getValue(key);
        if (value == null)
            return null;
        redisClient.delete(key);
        try {
            return new ChallengeNonce(value, ZonedDateTime.now().plusSeconds(DEFAULT_EXPIRATION_SECONDS));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decode ChallengeNonce from Redis", e);
        }
    }

    @Override
    public ChallengeNonce getAndRemoveImpl() {
        return getAndRemove("nonce");
    }

}
