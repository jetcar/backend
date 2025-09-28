package com.example.oidc.service;

import ee.sk.mid.MidClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.security.KeyStore;

@Configuration
public class MobileIdClientConfig {

    @Bean
    public MidClient midClient(@Value("${mid.client.host-url}") String midClientHostUrl,
            @Value("${mid.client.relying-party-uuid}") String midClientRelyingPartyUUID,
            @Value("${mid.client.relying-party-name}") String midClientRelyingPartyName,
            @Value("${mid.client.trust-store}") String trustStore,
            @Value("${mid.client.trust-store-password}") String trustStorePassword) {
        try {
            // Load trust store using absolute path
            Resource resource = new FileSystemResource(trustStore);
            KeyStore trustStoreInstance = KeyStore.getInstance("PKCS12");
            try (InputStream trustStoreStream = resource.getInputStream()) {
                trustStoreInstance.load(trustStoreStream, trustStorePassword.toCharArray());
            }

            return MidClient.newBuilder()
                    .withHostUrl(midClientHostUrl)
                    .withRelyingPartyUUID(midClientRelyingPartyUUID)
                    .withRelyingPartyName(midClientRelyingPartyName)
                    .withTrustStore(trustStoreInstance)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MidClient with trust store", e);
        }
    }
}
