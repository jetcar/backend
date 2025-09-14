package com.example.oidc;

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
            "openid profile email"
        );
        clients.put(client.getClientId(), client);
    }

    public OidcClient getClient(String clientId) {
        return clients.get(clientId);
    }

    public boolean isValidClient(String clientId) {
        return clients.containsKey(clientId);
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

class OidcClient {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String scope;

    public OidcClient(String clientId, String clientSecret, String redirectUri, String scope) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.scope = scope;
    }

    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
    public String getRedirectUri() { return redirectUri; }
    public String getScope() { return scope; }
}
