package com.example.oidc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
@RestController
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    // Log all incoming requests
    @Bean
    public OncePerRequestFilter requestLoggingFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
                    throws ServletException, IOException {
                StringBuilder logMsg = new StringBuilder();
                logMsg.append("Incoming request: ")
                      .append(request.getMethod())
                      .append(" ")
                      .append(request.getRequestURI());
                if (request.getQueryString() != null) {
                    logMsg.append("?").append(request.getQueryString());
                }
                // Log client IP
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty()) {
                    ip = request.getRemoteAddr();
                }
                logMsg.append(" | IP: ").append(ip);
                // Log all headers
                java.util.Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames.hasMoreElements()) {
                    logMsg.append(" | Headers: [");
                    boolean firstHeader = true;
                    while (headerNames.hasMoreElements()) {
                        String header = headerNames.nextElement();
                        if (!firstHeader) logMsg.append(", ");
                        logMsg.append(header).append("=").append(request.getHeader(header));
                        firstHeader = false;
                    }
                    logMsg.append("]");
                }
                // Log all parameters
                java.util.Enumeration<String> paramNames = request.getParameterNames();
                if (paramNames.hasMoreElements()) {
                    logMsg.append(" | Params: [");
                    boolean first = true;
                    while (paramNames.hasMoreElements()) {
                        String param = paramNames.nextElement();
                        if (!first) logMsg.append(", ");
                        logMsg.append(param).append("=").append(request.getParameter(param));
                        first = false;
                    }
                    logMsg.append("]");
                }
                log.info(logMsg.toString());
                filterChain.doFilter(request, response);
            }
        };
    }
    private final OidcSessionStore oidcSessionStore;
    private final OidcClientRegistry clientRegistry;

    @Autowired
    public App(OidcSessionStore oidcSessionStore, OidcClientRegistry clientRegistry) {
        this.oidcSessionStore = oidcSessionStore;
        this.clientRegistry = clientRegistry;
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    // Endpoint to start MobileId authentication
    // MobileId endpoints
    @PostMapping("/mobileid/start")
    public ResponseEntity<?> startMobileId(@RequestParam String country, @RequestParam String personalCode, @RequestParam String phoneNumber) {
        String sessionId = java.util.UUID.randomUUID().toString();
        String code = String.valueOf((int)(Math.random() * 9000) + 1000);
        // Store session in Redis using OidcSessionStore
        oidcSessionStore.storeMobileIdSession(sessionId, new MobileIdSession(false, country, personalCode, phoneNumber));
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("sessionId", sessionId);
        response.put("code", code);
        response.put("country", country);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mobileid/check")
    public ResponseEntity<?> checkMobileId(
            @RequestParam String sessionId,
            @RequestParam(required = false) String client_id,
            @RequestParam(required = false) String redirect_uri,
            @RequestParam(required = false) String response_type,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String nonce) {
        MobileIdSession session = oidcSessionStore.getMobileIdSession(sessionId);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("sessionId", sessionId);
        boolean complete = session != null && true;
        response.put("complete", complete);
        // OIDC client validation
        boolean validClient = client_id != null && clientRegistry.isValidClient(client_id);
        response.put("validClient", validClient);
        // Simulate user authorization (for demo, treat complete==true as authorized)
        boolean authorized = complete;
        response.put("authorized", authorized);
        // If authorized and client is valid, add redirectUrl for OIDC flow
        if (authorized && validClient && session != null) {
            OidcClient client = clientRegistry.getClient(client_id);
            if (client != null) {
                String code = java.util.UUID.randomUUID().toString();
                // Store user info for OIDC flow, including nonce
                OidcSessionStore.UserInfo user = new OidcSessionStore.UserInfo(session.personalCode, "MobileId User", "mobileid@example.com", session.country, session.phoneNumber, nonce);
                oidcSessionStore.storeCode(code, user);
                StringBuilder redirectUrl = new StringBuilder();
                redirectUrl.append(client.getRedirectUri()).append("?code=").append(code);
                if (state != null) {
                    redirectUrl.append("&state=").append(state);
                }
                response.put("redirectUrl", redirectUrl.toString());
            }
        }
        return ResponseEntity.ok(response);
    }

    // SmartId endpoints
    @PostMapping("/smartid/start")
    public ResponseEntity<?> startSmartId(@RequestParam String country, @RequestParam String personalCode) {
        String sessionId = java.util.UUID.randomUUID().toString();
        String code = String.valueOf((int)(Math.random() * 9000) + 1000);
        SmartIdSessionStore.sessions.put(sessionId, false);
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("sessionId", sessionId);
        response.put("code", code);
        response.put("country", country);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/smartid/check")
    public ResponseEntity<?> checkSmartId(@RequestParam String sessionId) {
        Boolean status = SmartIdSessionStore.sessions.get(sessionId);
        if (status == null) {
            return ResponseEntity.status(404).body("Session not found");
        }
        if (!status && Math.random() > 0.7) {
            SmartIdSessionStore.sessions.put(sessionId, true);
            status = true;
        }
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("sessionId", sessionId);
        response.put("complete", status);
        return ResponseEntity.ok(response);
    }

    // ...existing code...
    public static class MobileIdSession {
        public boolean complete;
        public String country;
        public String personalCode;
        public String phoneNumber;
        public MobileIdSession() {}
        public MobileIdSession(boolean complete, String country, String personalCode, String phoneNumber) {
            this.complete = complete;
            this.country = country;
            this.personalCode = personalCode;
            this.phoneNumber = phoneNumber;
        }
    }
    static class SmartIdSessionStore {
        static java.util.Map<String, Boolean> sessions = new java.util.concurrent.ConcurrentHashMap<>();
    }
}
