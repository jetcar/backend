package com.example.oidc.config;

import eu.webeid.security.validator.AuthTokenValidator;
import eu.webeid.security.validator.AuthTokenValidatorBuilder;
import eu.webeid.security.exceptions.JceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import java.io.InputStream;
import java.net.URI;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties(WebEidProperties.class)
public class WebEidAuthTokenConfig {

    private static final Logger log = LoggerFactory.getLogger(WebEidAuthTokenConfig.class);

    @Bean
    public AuthTokenValidator tokenValidator(WebEidProperties props) throws JceException {
        String origin = props.getAllowedOrigin();
        if (origin == null || origin.isBlank()) {
            throw new IllegalStateException("webeid.allowed-origin must be configured in application.yml");
        }
        return new AuthTokenValidatorBuilder()
                .withSiteOrigin(URI.create(origin.trim()))
                .withTrustedCertificateAuthorities(trustedIntermediateCACertificates(props.getCaCerts()))
                .build();
    }

    private X509Certificate[] trustedIntermediateCACertificates(List<String> paths) {
        try {
            List<X509Certificate> result = new ArrayList<>();
            if (paths == null) {
                return result.toArray(new X509Certificate[result.size()]);
            }
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            for (String p : paths) {
                if (p == null || p.isBlank())
                    continue;
                Resource res = new FileSystemResource(p.trim());
                if (!res.exists())
                    continue;
                try (InputStream in = res.getInputStream()) {
                    X509Certificate cert = (X509Certificate) cf.generateCertificate(in);
                    log.info("Loaded Web-eID trusted CA from '{}' DN='{}'",
                            p.trim(), cert.getSubjectX500Principal().getName());
                    result.add(cert);
                }
            }
            return result.toArray(new X509Certificate[result.size()]);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Web-eID trusted CA certificates", e);
        }
    }
}
