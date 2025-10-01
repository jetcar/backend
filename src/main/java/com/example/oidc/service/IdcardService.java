package com.example.oidc.service;

import com.example.oidc.dto.IdCardSession;
import com.example.oidc.storage.OidcClient;
import com.example.oidc.storage.OidcClientRegistry;
import com.example.oidc.storage.OidcSessionStore;
import com.example.oidc.storage.UserInfo;
import com.example.oidc.util.PersonalCodeHelper;
import eu.webeid.security.authtoken.WebEidAuthToken;
import eu.webeid.security.certificate.CertificateData;
import eu.webeid.security.challenge.ChallengeNonce;
import eu.webeid.security.challenge.ChallengeNonceGenerator;
import eu.webeid.security.validator.AuthTokenValidator;
import eu.webeid.security.exceptions.AuthTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class IdcardService {

    private final ChallengeNonceGenerator challengeNonceGenerator;
    private final AuthTokenValidator authTokenValidator;
    private final OidcClientRegistry clientRegistry;
    private final OidcSessionStore oidcSessionStore;

    @Autowired
    public IdcardService(
            ChallengeNonceGenerator challengeNonceGenerator,
            AuthTokenValidator authTokenValidator,
            OidcClientRegistry clientRegistry,
            OidcSessionStore oidcSessionStore) {
        this.challengeNonceGenerator = challengeNonceGenerator;
        this.authTokenValidator = authTokenValidator;
        this.clientRegistry = clientRegistry;
        this.oidcSessionStore = oidcSessionStore;
    }

    public Map<String, Object> createChallenge(
            String clientId,
            String redirectUri,
            String state,
            String nonce) {
        ChallengeNonce challengeNonce = challengeNonceGenerator.generateAndStoreNonce();
        Map<String, Object> resp = new HashMap<>();
        String sessionId = java.util.UUID.randomUUID().toString();
        oidcSessionStore.storeIdCardSession(
                sessionId,
                new IdCardSession(false, challengeNonce.getBase64EncodedNonce()));
        resp.put("nonce", challengeNonce.getBase64EncodedNonce());
        resp.put("sessionId", sessionId);
        return resp;
    }

    public ResponseEntity<?> login(
            Map<String, Object> body,
            String clientId,
            String redirectUri,
            String state,
            String nonce,
            String sessionId) throws CertificateEncodingException {
        Map<String, Object> resp = new HashMap<>();
        Object authTokenObj = body.get("authToken");
        WebEidAuthToken authToken = null;
        if (authTokenObj instanceof Map) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String json = mapper.writeValueAsString(authTokenObj);
                authToken = mapper.readValue(json, WebEidAuthToken.class);
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

        IdCardSession idCardSession = null;
        if (sessionId != null && !sessionId.isBlank()) {
            idCardSession = oidcSessionStore.getIdCardSession(sessionId);
            if (idCardSession == null) {
                resp.put("error", "Session not found or expired");
                return ResponseEntity.badRequest().body(resp);
            }
        }

        X509Certificate certificate;
        try {
            certificate = authTokenValidator.validate(authToken, idCardSession.getChallengeNonce());
        } catch (AuthTokenException e) {
            resp.put("error", "Web eID token validation failed");
            resp.put("message", e.getMessage());
            return ResponseEntity.status(401).body(resp);
        } catch (Exception e) {
            resp.put("error", "Unexpected error");
            resp.put("message", e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }

        String country = CertificateData.getSubjectCountryCode(certificate).orElseThrow();
        String subject = CertificateData.getSubjectIdCode(certificate).orElseThrow()
                .replaceAll("PNO", "")
                .replaceAll(country + "-", "");
        String givenName = CertificateData.getSubjectGivenName(certificate).orElseThrow();
        String surname = CertificateData.getSubjectSurname(certificate).orElseThrow();
        LocalDate dateOfBirth = PersonalCodeHelper.getDateOfBirth(subject);

        String certBase64;
        try {
            certBase64 = java.util.Base64.getEncoder().encodeToString(certificate.getEncoded());
        } catch (Exception e) {
            resp.put("error", "Failed to encode certificate");
            resp.put("message", e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }

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
        user.setCert(certBase64);
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
