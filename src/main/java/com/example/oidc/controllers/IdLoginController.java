package com.example.oidc.controllers;

import com.example.oidc.dto.IdCardSession;
import com.example.oidc.storage.OidcClient;
import com.example.oidc.storage.OidcClientRegistry;
import com.example.oidc.storage.OidcSessionStore;
import com.example.oidc.storage.UserInfo;
import com.example.oidc.util.PersonalCodeHelper;
import com.example.oidc.storage.RedisClient;
import eu.webeid.security.authtoken.WebEidAuthToken;
import eu.webeid.security.certificate.CertificateData;
import eu.webeid.security.challenge.ChallengeNonce;
import eu.webeid.security.challenge.ChallengeNonceGenerator;
import eu.webeid.security.validator.AuthTokenValidator;
import eu.webeid.security.exceptions.AuthTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/idlogin")
public class IdLoginController {

    private final ChallengeNonceGenerator challengeNonceGenerator;
    private final AuthTokenValidator authTokenValidator;
    private final OidcClientRegistry clientRegistry;
    private final OidcSessionStore oidcSessionStore;

    @Autowired
    public IdLoginController(
            ChallengeNonceGenerator challengeNonceGenerator,
            AuthTokenValidator authTokenValidator,
            OidcClientRegistry clientRegistry,
            OidcSessionStore oidcSessionStore,
            RedisClient redisClient) {
        this.challengeNonceGenerator = challengeNonceGenerator;
        this.authTokenValidator = authTokenValidator;
        this.clientRegistry = clientRegistry;
        this.oidcSessionStore = oidcSessionStore;
    }

    @GetMapping("/challenge")
    public Map<String, Object> challenge(
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "nonce", required = false) String nonce) {
        // Optionally store OIDC params in session or Redis if needed for later

        ChallengeNonce challengeNonce = challengeNonceGenerator.generateAndStoreNonce();
        Map<String, Object> resp = new HashMap<>();

        String sessionId = java.util.UUID.randomUUID().toString();

        // Store the challenge nonce in an IdCardSession for this sessionId
        oidcSessionStore.storeIdCardSession(
                sessionId,
                new IdCardSession(false, challengeNonce.getBase64EncodedNonce()));

        resp.put("nonce", challengeNonce.getBase64EncodedNonce());
        resp.put("sessionId", sessionId);
        return resp;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(
            @RequestBody Map<String, Object> body,
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "nonce", required = false) String nonce,
            @RequestParam(value = "sessionId", required = false) String sessionId) throws CertificateEncodingException {
        Map<String, Object> resp = new HashMap<>();
        // Extract WebEidAuthToken from JSON body
        Object authTokenObj = body.get("authToken");
        eu.webeid.security.authtoken.WebEidAuthToken authToken = null;
        if (authTokenObj instanceof Map) {
            try {
                // Convert the map to JSON and then to WebEidAuthToken
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String json = mapper.writeValueAsString(authTokenObj);
                authToken = mapper.readValue(json, eu.webeid.security.authtoken.WebEidAuthToken.class);
            } catch (Exception e) {
                resp.put("error", "Invalid authToken format");
                resp.put("message", e.getMessage());
                return ResponseEntity.badRequest().body(resp);
            }
        }
        if (authToken == null) {
            resp.put("error", "Missing or invalid authToken");
            return ResponseEntity.badRequest().body(resp);
        }

        // Load IdCardSession using sessionId
        com.example.oidc.dto.IdCardSession idCardSession = null;
        if (sessionId != null && !sessionId.isBlank()) {
            idCardSession = oidcSessionStore.getIdCardSession(sessionId);
            if (idCardSession == null) {
                resp.put("error", "Session not found or expired");
                return ResponseEntity.badRequest().body(resp);
            }
        }

        X509Certificate result;
        try {
            result = authTokenValidator.validate(authToken, idCardSession.getChallengeNonce());
        } catch (AuthTokenException e) {
            resp.put("error", "Web eID token validation failed");
            resp.put("message", e.getMessage());
            return ResponseEntity.status(401).body(resp);
        } catch (Exception e) {
            resp.put("error", "Unexpected error");
            resp.put("message", e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }

        // Extract user info from result
        String subject = CertificateData.getSubjectIdCode(result).orElseThrow();
        String givenName = CertificateData.getSubjectGivenName(result).orElseThrow();
        String surname = CertificateData.getSubjectSurname(result).orElseThrow();
        String country = CertificateData.getSubjectCountryCode(result).orElseThrow();
        LocalDate dateOfBirth = PersonalCodeHelper.getDateOfBirth(subject);

        // Validate OIDC client
        OidcClient client = clientRegistry.isValidClient(clientId, redirectUri);
        if (client == null) {
            resp.put("error", "Invalid client or redirect_uri");
            return ResponseEntity.badRequest().body(resp);
        }

        String code = java.util.UUID.randomUUID().toString();
        UserInfo user = new UserInfo(
                subject,
                givenName,
                surname,
                country,
                dateOfBirth,
                null,
                nonce);
        oidcSessionStore.storeCode(code, user);

        StringBuilder redirect = new StringBuilder(client.getRedirectUri())
                .append("?code=").append(code);
        if (state != null) {
            redirect.append("&state=").append(state);
        }
        resp.put("redirectUrl", redirect.toString());
        return ResponseEntity.ok(resp);
    }
}
