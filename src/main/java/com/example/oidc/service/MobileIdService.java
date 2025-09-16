package com.example.oidc.service;

import com.example.oidc.OidcClient;
import com.example.oidc.OidcClientRegistry;
import com.example.oidc.dto.MobileIdSession;
import com.example.oidc.storage.OidcSessionStore;
import com.example.oidc.storage.UserInfo;
import ee.sk.mid.MidAuthenticationHashToSign;
import ee.sk.mid.MidAuthenticationResponseValidator;
import ee.sk.mid.MidAuthenticationResult;
import ee.sk.mid.MidClient;
import ee.sk.mid.MidHashType;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.MidAuthentication;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

@Service
public class MobileIdService {

    private static final Logger log = LoggerFactory.getLogger(MobileIdService.class);

    private final MidClient midClient;
    private final OidcSessionStore oidcSessionStore;
    private final OidcClientRegistry clientRegistry;

    @Value("${mid.client.trust-store}")
    private String trustStorePath;

    @Value("${mid.client.trust-store-password}")
    private String trustStorePassword;

    @Autowired
    public MobileIdService(MidClient midClient, OidcSessionStore oidcSessionStore, OidcClientRegistry clientRegistry) {
        this.midClient = midClient;
        this.oidcSessionStore = oidcSessionStore;
        this.clientRegistry = clientRegistry;
    }

    public Map<String, String> startMobileId(String country, String personalCode, String phoneNumber, String clientId,
            String redirectUri) {
        OidcClient client = clientRegistry.isValidClient(clientId, redirectUri);
        if (client == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid client");
            return errorResponse;
        }
        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();
        String verificationCode = authenticationHash.calculateVerificationCode();

        MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
                .withPhoneNumber(phoneNumber)
                .withNationalIdentityNumber(personalCode)
                .withHashToSign(authenticationHash)
                .withLanguage(ee.sk.mid.MidLanguage.ENG)
                .build();

        MidAuthenticationResponse response = midClient.getMobileIdConnector().authenticate(request);

        String sessionId = response.getSessionID();
        log.info("Authentication session ID: " + sessionId);

        oidcSessionStore.storeMobileIdSession(sessionId,
                new MobileIdSession(false, country, personalCode, phoneNumber, authenticationHash.getHashInBase64()));

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("sessionId", sessionId);
        responseBody.put("code", verificationCode);
        responseBody.put("country", country);
        return responseBody;
    }

    public Map<String, Object> checkMobileId(String sessionId, String clientId, String redirectUri, String responseType,
            String scope, String state, String nonce) {
        MobileIdSession session = oidcSessionStore.getMobileIdSession(sessionId);
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);

        boolean complete = false;
        OidcClient client = clientRegistry.isValidClient(clientId, redirectUri);
        boolean validClient = client != null;
        boolean authorized = false;
        String error = null;

        if (!validClient) {
            error = "Invalid client";
            response.put("complete", complete);
            response.put("validClient", false);
            response.put("authorized", authorized);
            response.put("error", error);
            return response;
        }

        if (session != null) {
            try {
                MidSessionStatus sessionStatus = midClient.getSessionStatusPoller()
                        .fetchFinalSessionStatus(sessionId, "/authentication/session/{sessionId}");

                MidAuthenticationHashToSign authenticationHashToSign = MidAuthenticationHashToSign.newBuilder()
                        .withHashType(MidHashType.SHA256)
                        .withHashInBase64(session.authenticationHash)
                        .build();

                MidAuthentication authentication = midClient.createMobileIdAuthentication(sessionStatus,
                        authenticationHashToSign);

                // Load truststore for validator using FileSystemResource and config values
                Resource resource = new FileSystemResource(trustStorePath);
                KeyStore trustStoreInstance = KeyStore.getInstance("PKCS12");
                try (InputStream trustStoreStream = resource.getInputStream()) {
                    trustStoreInstance.load(trustStoreStream, trustStorePassword.toCharArray());
                }

                MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(
                        trustStoreInstance);
                MidAuthenticationResult authenticationResult = validator.validate(authentication);

                if (authenticationResult.isValid()) {
                    complete = true;
                    authorized = true;
                } else {
                    error = String.valueOf(authenticationResult.getErrors());
                    log.error("MobileId authentication errors for sessionId {}: {}", sessionId,
                            authenticationResult.getErrors());
                }
            } catch (Exception e) {
                error = e.getMessage();
                log.error("Error polling MobileId status for sessionId {}: {}", sessionId, e.getMessage());
            }
        } else {
            error = "Session not found";
        }

        response.put("complete", complete);
        response.put("validClient", validClient);
        response.put("authorized", authorized);
        if (error != null) {
            response.put("error", error);
        }

        if (authorized && validClient && session != null && client != null) {
            String code = java.util.UUID.randomUUID().toString();
            UserInfo user = new UserInfo(session.personalCode, "MobileId User", "mobileid@example.com",
                    session.country, session.phoneNumber, nonce);
            oidcSessionStore.storeCode(code, user);
            StringBuilder redirectUrl = new StringBuilder()
                    .append(client.getRedirectUri()).append("?code=").append(code);
            if (state != null) {
                redirectUrl.append("&state=").append(state);
            }
            response.put("redirectUrl", redirectUrl.toString());
        }
        return response;
    }
}