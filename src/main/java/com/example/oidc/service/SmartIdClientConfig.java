package com.example.oidc.service;

import ee.sk.smartid.SmartIdClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.security.KeyStore;

@Configuration
public class SmartIdClientConfig {

    @Bean
    public SmartIdClient smartIdClient(@Value("${smartid.client.host-url}") String smartIdClientHostUrl,
            @Value("${smartid.client.relying-party-uuid}") String smartIdClientRelyingPartyUUID,
            @Value("${smartid.client.relying-party-name}") String smartIdClientRelyingPartyName,
            @Value("${smartid.client.trust-store}") String trustStore,
            @Value("${smartid.client.trust-store-password}") String trustStorePassword) {
        try {
            // Load trust store using FileSystemResource
            Resource resource = new FileSystemResource(trustStore);
            KeyStore trustStoreInstance = KeyStore.getInstance("PKCS12");
            try (InputStream trustStoreStream = resource.getInputStream()) {
                trustStoreInstance.load(trustStoreStream, trustStorePassword.toCharArray());
            }

            SmartIdClient client = new SmartIdClient();
            client.setRelyingPartyUUID(smartIdClientRelyingPartyUUID);
            client.setRelyingPartyName(smartIdClientRelyingPartyName);
            client.setHostUrl(smartIdClientHostUrl);
            client.setTrustStore(trustStoreInstance);
            return client;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SmartIdClient with trust store", e);
        }
    }
}
