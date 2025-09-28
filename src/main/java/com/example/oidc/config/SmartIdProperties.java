package com.example.oidc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "smartid.client")
public class SmartIdProperties {

    private List<String> caCerts;

    public List<String> getCaCerts() {
        return caCerts;
    }

    public void setCaCerts(List<String> caCerts) {
        this.caCerts = caCerts;
    }
}
