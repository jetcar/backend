package com.example.oidc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@SpringBootApplication
@RestController
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    // Endpoint to start MobileId authentication
    // MobileId endpoints
    @PostMapping("/mobileid/start")
    public ResponseEntity<?> startMobileId(@RequestParam String country, @RequestParam String personalCode, @RequestParam String phoneNumber) {
        String sessionId = java.util.UUID.randomUUID().toString();
        String code = String.valueOf((int)(Math.random() * 9000) + 1000);
        MobileIdSessionStore.sessions.put(sessionId, false);
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("sessionId", sessionId);
        response.put("code", code);
        response.put("country", country);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mobileid/check")
    public ResponseEntity<?> checkMobileId(@RequestParam String sessionId) {
        Boolean status = MobileIdSessionStore.sessions.get(sessionId);
        if (status == null) {
            return ResponseEntity.status(404).body("Session not found");
        }
        if (!status && Math.random() > 0.7) {
            MobileIdSessionStore.sessions.put(sessionId, true);
            status = true;
        }
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("sessionId", sessionId);
        response.put("complete", status);
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

    // Simple in-memory stores for demo
    static class MobileIdSessionStore {
        static java.util.Map<String, Boolean> sessions = new java.util.concurrent.ConcurrentHashMap<>();
    }
    static class SmartIdSessionStore {
        static java.util.Map<String, Boolean> sessions = new java.util.concurrent.ConcurrentHashMap<>();
    }
}
