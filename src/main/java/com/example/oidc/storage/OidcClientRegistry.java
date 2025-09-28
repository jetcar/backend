package com.example.oidc.storage;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class OidcClientRegistry {
    private final Map<String, OidcClient> clients = new HashMap<>();

    public OidcClientRegistry() {
        // Hardcoded client example
        OidcClient client = new OidcClient(
                "demo-client-id",
                "demo-client-secret",
                "https://localhost:8082/login/oauth2/code/demo",
                "openid profile email");
        clients.put(client.getClientId(), client);
    }

    public OidcClient getClient(String clientId) {
        return clients.get(clientId);
    }

    public OidcClient isValidClient(String clientId, String redirectUri) {
        OidcClient client = clients.get(clientId);
        return (client != null && client.getRedirectUri().equals(redirectUri)) ? client : null;
    }

    public OidcClient getClientByReturnUri(String returnUri) {
        for (OidcClient client : clients.values()) {
            if (client.getRedirectUri().equals(returnUri)) {
                return client;
            }
        }
        return null;
    }
}
