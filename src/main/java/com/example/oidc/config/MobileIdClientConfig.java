package com.example.oidc.config;

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

    @Value("${mid.client.host-url}")
    private String midClientHostUrl;

    @Value("${mid.client.relying-party-uuid}")
    private String midClientRelyingPartyUUID;

    @Value("${mid.client.relying-party-name}")
    private String midClientRelyingPartyName;

    @Value("${mid.client.api-trust-store}")
    private String trustStore;

    @Value("${mid.client.api-trust-store-password}")
    private String trustStorePassword;

    @Bean
    public MidClient midClient() {
        try {
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
