package com.example.oidc.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.JWKSet;

@RestController
public class JwksController {
    private final JWKSet jwkSet;

    public JwksController() throws Exception {
        // Load public key from keystore.p12 (alias: springboot)
        java.security.KeyStore ks = java.security.KeyStore.getInstance("PKCS12");
        try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("keystore.p12")) {
            ks.load(is, "changeit".toCharArray());
        }
        java.security.cert.Certificate cert = ks.getCertificate("springboot");
        java.security.interfaces.RSAPublicKey publicKey = (java.security.interfaces.RSAPublicKey) cert.getPublicKey();
        RSAKey rsaJwk = new RSAKey.Builder(publicKey)
            .keyID("springboot")
            .build();
        jwkSet = new JWKSet(rsaJwk);
    }

    @GetMapping("/.well-known/jwks.json")
    public String getJwks() {
        return jwkSet.toJSONObject().toString();
    }
}
