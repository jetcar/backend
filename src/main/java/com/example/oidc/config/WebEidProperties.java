package com.example.oidc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "webeid")
public class WebEidProperties {

    private List<String> caCerts;
    private String allowedOrigin; // optional

    public List<String> getCaCerts() {
        return caCerts;
    }

    public void setCaCerts(List<String> caCerts) {
        this.caCerts = caCerts;
    }

    public String getAllowedOrigin() {
        return allowedOrigin;
    }

    public void setAllowedOrigin(String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }
}
