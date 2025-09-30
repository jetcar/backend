package com.example.oidc.config;

import eu.webeid.security.challenge.ChallengeNonceGenerator;
import eu.webeid.security.challenge.ChallengeNonceGeneratorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.oidc.storage.EmptyChallengeNonceStore;
import com.example.oidc.storage.RedisClient;

@Configuration
public class WebEidChallengeConfig {

    @Bean
    public ChallengeNonceGenerator challengeNonceGenerator(RedisClient redisClient) {
        return new ChallengeNonceGeneratorBuilder()
                .withChallengeNonceStore(new EmptyChallengeNonceStore())
                .build();
    }
}
