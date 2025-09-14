package com.example.oidc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

@RestController
public class OidcDiscoveryController {
    @GetMapping("/.well-known/openid-configuration")
    public Map<String, Object> discovery() {
        Map<String, Object> config = new HashMap<>();
    config.put("issuer", "https://localhost:8443");
    config.put("authorization_endpoint", "https://localhost:8443/authorize");
    config.put("token_endpoint", "https://localhost:8443/token");
    config.put("userinfo_endpoint", "https://localhost:8443/userinfo");
    config.put("jwks_uri", "https://localhost:8443/.well-known/jwks.json");
        config.put("response_types_supported", new String[]{"code", "id_token", "token"});
        config.put("subject_types_supported", new String[]{"public"});
        config.put("id_token_signing_alg_values_supported", new String[]{"RS256"});
        config.put("scopes_supported", new String[]{"openid", "profile", "email"});
        config.put("token_endpoint_auth_methods_supported", new String[]{"client_secret_basic"});
        return config;
    }
}
