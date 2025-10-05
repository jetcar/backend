package com.example.oidc.config;

import eu.webeid.security.validator.AuthTokenValidator;
import eu.webeid.security.validator.AuthTokenValidatorBuilder;
import eu.webeid.security.exceptions.JceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.beans.factory.annotation.Value;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebEidAuthTokenConfig {

    private static final Logger log = LoggerFactory.getLogger(WebEidAuthTokenConfig.class);

    @Value("${webeid.ca-keystore:config/idcard.p12}")
    private String keystorePath;

    @Value("${webeid.ca-keystore-password:}")
    private String keystorePassword;

    @Value("${oidc.issuer}")
    private String oidcIssuer;

    @Bean
    public AuthTokenValidator tokenValidator() throws JceException {
        String origin = oidcIssuer;
        if (origin == null || origin.isBlank()) {
            throw new IllegalStateException("oidc.issuer must be configured in application.yml");
        }
        return new AuthTokenValidatorBuilder()
                .withSiteOrigin(URI.create(origin.trim()))
                .withTrustedCertificateAuthorities(loadTrustedCAsFromKeystore())
                .build();
    }

    private X509Certificate[] loadTrustedCAsFromKeystore() {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (InputStream is = new FileSystemResource(keystorePath).getInputStream()) {
                ks.load(is, keystorePassword.toCharArray());
            }
            List<X509Certificate> result = new ArrayList<>();
            for (String alias : java.util.Collections.list(ks.aliases())) {
                Certificate cert = ks.getCertificate(alias);
                if (cert instanceof X509Certificate) {
                    log.info("Loaded Web-eID trusted CA from keystore '{}' alias='{}' DN='{}'",
                            keystorePath, alias, ((X509Certificate) cert).getSubjectX500Principal().getName());
                    result.add((X509Certificate) cert);
                }
            }
            return result.toArray(new X509Certificate[0]);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Web-eID trusted CA certificates from idcard.p12", e);
        }
    }
}
