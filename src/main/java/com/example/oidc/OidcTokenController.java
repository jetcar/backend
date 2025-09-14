package com.example.oidc;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.oidc.OidcSessionStore.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import org.springframework.beans.factory.annotation.Value;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

@RestController
public class OidcTokenController {
    private final OidcSessionStore oidcSessionStore;
    private final OidcClientRegistry clientRegistry;
    private PrivateKey jwtPrivateKey;
    @Value("${server.ssl.key-store-password}")
    private String keystorePassword;

    @Autowired
    public OidcTokenController(OidcSessionStore oidcSessionStore, OidcClientRegistry clientRegistry) {
        this.oidcSessionStore = oidcSessionStore;
        this.clientRegistry = clientRegistry;
    }

    @jakarta.annotation.PostConstruct
    public void loadPrivateKey() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("keystore.p12")) {
            ks.load(is, keystorePassword.toCharArray());
        }
        jwtPrivateKey = (PrivateKey) ks.getKey("springboot", keystorePassword.toCharArray());
    }

    @PostMapping("/token")
    public Map<String, Object> token(@RequestParam Map<String, String> params, HttpServletResponse servletResponse) {
        String code = params.get("code");
        Map<String, Object> response = new HashMap<>();
        if (code == null || code.isEmpty()) {
            response.put("error", "Missing authorization code");
            return response;
        }
        String redirectUri = params.get("redirect_uri");
        String clientId = null;
        if (redirectUri != null && !redirectUri.isEmpty()) {
            OidcClient client = clientRegistry.getClientByReturnUri(redirectUri);
            if (client != null) {
                clientId = client.getClientId();
            }
        }
        if (clientId == null || clientId.isEmpty()) {
            response.put("error", "Missing client_id parameter and could not resolve from redirect_uri");
            return response;
        }
        UserInfo user = oidcSessionStore.getUserByCode(code);
        if (user == null) {
            response.put("error", "Invalid or expired authorization code");
            return response;
        }
        String accessToken = java.util.UUID.randomUUID().toString();
        oidcSessionStore.storeToken(accessToken, user);
        // Generate a real JWT for id_token
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("https://localhost:8443")
                .subject(user.getSub())
                .audience(clientId)
                .claim("name", user.getName())
                .claim("email", user.getEmail())
                .claim("country", user.getCountry())
                .claim("phone_number", user.getPhoneNumber())
                .expirationTime(new java.util.Date(System.currentTimeMillis() + 3600 * 1000))
                .issueTime(new java.util.Date())
                .build();
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID("springboot")
                .build();
            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(new RSASSASigner(jwtPrivateKey));
            String idToken = signedJWT.serialize();
            response.put("access_token", accessToken);
            response.put("token_type", "Bearer");
            response.put("expires_in", 3600);
            response.put("id_token", idToken);
            servletResponse.addHeader("Set-Cookie", "id_token=" + idToken + "; Path=/; HttpOnly; Secure");
        } catch (Exception e) {
            response.put("error", "Failed to generate id_token: " + e.getMessage());
        }
        return response;
    }
}
