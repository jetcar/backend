package com.example.oidc.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.oidc.storage.UserInfo;
import com.example.oidc.storage.OidcClient;
import com.example.oidc.storage.OidcClientRegistry;
import com.example.oidc.storage.OidcSessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import java.security.KeyStore;
import java.security.PrivateKey;

@RestController
public class OidcTokenController {
    private final OidcSessionStore oidcSessionStore;
    private final OidcClientRegistry clientRegistry;
    private PrivateKey jwtPrivateKey;
    @Value("${server.ssl.key-store-password}")
    private String keystorePassword;
    @Value("${oidc.issuer:https://localhost:8443}")
    private String issuer;

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
        // Generate a proper JWT for access_token
        String scope = params.getOrDefault("scope", "openid profile email");
        String accessToken = null;
        try {
            JWTClaimsSet accessTokenClaims = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .subject(user.getSub())
                    .audience(clientId)
                    .claim("scope", scope)
                    .expirationTime(new java.util.Date(System.currentTimeMillis() + 3600 * 1000))
                    .issueTime(new java.util.Date())
                    .build();
            JWSHeader accessTokenHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID("springboot")
                    .build();
            SignedJWT accessSignedJWT = new SignedJWT(accessTokenHeader, accessTokenClaims);
            accessSignedJWT.sign(new RSASSASigner(jwtPrivateKey));
            accessToken = accessSignedJWT.serialize();
            oidcSessionStore.storeToken(accessToken, user);

            // Generate a real JWT for id_token
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .subject(user.getSub())
                    .audience(clientId)
                    .claim("name", user.getGivenName())
                    .claim("surname", user.getSurname())
                    .claim("country", user.getCountry())
                    .claim("phone_number", user.getPhoneNumber())
                    .expirationTime(new java.util.Date(System.currentTimeMillis() + 3600 * 1000))
                    .issueTime(new java.util.Date());

            if (user.getNonce() != null && !user.getNonce().isEmpty()) {
                claimsBuilder.claim("nonce", user.getNonce());
            }

            JWTClaimsSet claims = claimsBuilder.build();
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
