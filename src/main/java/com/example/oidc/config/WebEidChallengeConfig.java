package com.example.oidc.config;

import eu.webeid.security.challenge.ChallengeNonce;
import eu.webeid.security.challenge.ChallengeNonceGenerator;
import eu.webeid.security.challenge.ChallengeNonceGeneratorBuilder;
import eu.webeid.security.challenge.ChallengeNonceStore;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.oidc.storage.RedisClient;

@Configuration
public class WebEidChallengeConfig {

    @Bean
    public ChallengeNonceGenerator challengeNonceGenerator(RedisClient redisClient) {
        return new ChallengeNonceGeneratorBuilder()
                .withChallengeNonceStore(new EmptyChallengeNonceStore())
                .build();
    }

    public class EmptyChallengeNonceStore implements ChallengeNonceStore {

    public EmptyChallengeNonceStore() {
        // No-op constructor
    }

    @Override
    public void put(ChallengeNonce challengeNonce) {
        // No-op
    }

    // Custom method to get and remove by nonce value (returns null)
    public ChallengeNonce getAndRemove(String nonce) {
        return null;
    }

    @Override
    public ChallengeNonce getAndRemoveImpl() {
        return null;
    }
}}





