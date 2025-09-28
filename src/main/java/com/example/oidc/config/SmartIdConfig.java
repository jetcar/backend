package com.example.oidc.config;

import ee.sk.smartid.AuthenticationResponseValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary; // added

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Configuration
@EnableConfigurationProperties(SmartIdProperties.class)
public class SmartIdConfig {

    private static final Logger log = LoggerFactory.getLogger(SmartIdConfig.class);

    @Bean
    @Primary
    public AuthenticationResponseValidator authenticationResponseValidator(SmartIdProperties props) {
        try {
            AuthenticationResponseValidator validator = new AuthenticationResponseValidator();

            if (props.getCaCerts() != null) {
                for (String path : props.getCaCerts()) {
                    if (path == null || path.isBlank())
                        continue;
                    Resource res = new FileSystemResource(path.trim());
                    if (!res.exists())
                        continue;
                    try (InputStream in = res.getInputStream()) {
                        X509Certificate ca = (X509Certificate) CertificateFactory.getInstance("X.509")
                                .generateCertificate(in);
                        // Log the DN (Subject) of the loaded certificate
                        log.info("Loaded Smart-ID CA certificate from '{}' DN='{}'",
                                path.trim(), ca.getSubjectX500Principal().getName());
                        validator.addTrustedCACertificate(ca);
                    }
                }
            }

            return validator;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Smart-ID CA certificates from smartid.client.ca-certs", e);
        }
    }
}