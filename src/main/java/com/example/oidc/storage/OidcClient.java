package com.example.oidc.storage;

public class OidcClient {
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

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getScope() {
        return scope;
    }
}