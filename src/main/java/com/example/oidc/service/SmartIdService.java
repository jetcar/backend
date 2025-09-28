package com.example.oidc.service;

import com.example.oidc.dto.SmartIdSession;
import com.example.oidc.storage.OidcClient;
import com.example.oidc.storage.OidcClientRegistry;
import com.example.oidc.storage.OidcSessionStore;
import com.example.oidc.storage.UserInfo;
import ee.sk.smartid.AuthenticationHash;
import ee.sk.smartid.AuthenticationIdentity;
import ee.sk.smartid.AuthenticationResponseValidator;
import ee.sk.smartid.HashType;
import ee.sk.smartid.SmartIdClient;
import ee.sk.smartid.SmartIdAuthenticationResponse;
import ee.sk.smartid.rest.dao.SemanticsIdentifier;
import ee.sk.smartid.rest.dao.Interaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmartIdService {

    private final OidcSessionStore oidcSessionStore;
    private final OidcClientRegistry clientRegistry;
    private final SmartIdClient smartIdClient;
    private final AuthenticationResponseValidator authenticationResponseValidator; // singleton instance

    @Autowired
    public SmartIdService(OidcSessionStore oidcSessionStore, OidcClientRegistry clientRegistry,
            SmartIdClient smartIdClient,
            @Qualifier("authenticationResponseValidator") AuthenticationResponseValidator authenticationResponseValidator) {
        this.oidcSessionStore = oidcSessionStore;
        this.clientRegistry = clientRegistry;
        this.smartIdClient = smartIdClient;
        this.authenticationResponseValidator = authenticationResponseValidator; // injected bean
    }

    public Map<String, String> startSmartId(String country, String personalCode) {
        // For security reasons a new hash value must be created for each new
        // authentication request

        AuthenticationHash authenticationHash = new AuthenticationHash();
        authenticationHash.setHashType(HashType.SHA512);
        authenticationHash = authenticationHash.generateRandomHash();
        String verificationCode = authenticationHash.calculateVerificationCode();

        String sessionId = java.util.UUID.randomUUID().toString();

        oidcSessionStore.storeSmartIdSession(sessionId,
                new SmartIdSession(false, country, personalCode, authenticationHash.getHashInBase64()));

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("sessionId", sessionId);
        responseBody.put("code", verificationCode);
        responseBody.put("country", country);
        return responseBody;
    }

    public Map<String, Object> checkSmartId(String sessionId, String clientId, String redirectUri, String responseType,
            String scope, String state, String nonce) {
        SmartIdSession session = oidcSessionStore.getSmartIdSession(sessionId);
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);

        if (session == null) {
            response.put("complete", false);
            response.put("validClient", false);
            response.put("authorized", false);
            response.put("error", "Session not found");
            return response;
        }

        // Map country names to ISO codes
        String countryCode;
        switch (session.getCountry().toLowerCase()) {
            case "estonia":
                countryCode = "EE";
                break;
            case "latvia":
                countryCode = "LV";
                break;
            case "lithuania":
                countryCode = "LT";
                break;
            default:
                countryCode = session.getCountry().toUpperCase();
        }

        SemanticsIdentifier semanticsIdentifier = new SemanticsIdentifier(
                SemanticsIdentifier.IdentityType.PNO,
                SemanticsIdentifier.CountryCode.valueOf(countryCode),
                session.getPersonalCode());

        AuthenticationHash authenticationHash = new AuthenticationHash();
        authenticationHash.setHashInBase64(session.getAuthenticationHash());
        authenticationHash.setHashType(HashType.SHA512);

        SmartIdAuthenticationResponse smartIdresponse = smartIdClient
                .createAuthentication()
                .withSemanticsIdentifier(semanticsIdentifier)
                .withAuthenticationHash(authenticationHash)
                .withCertificateLevel("QUALIFIED")
                .withAllowedInteractionsOrder(
                        Collections.singletonList(Interaction.displayTextAndPIN("Log in to self-service?")))
                .withShareMdClientIpAddress(true)
                .authenticate();

        AuthenticationIdentity authIdentity;
        try {

            authIdentity = authenticationResponseValidator.validate(smartIdresponse);
        } catch (Exception e) {
            response.put("complete", false);
            response.put("validClient", false);
            response.put("authorized", false);
            response.put("error", e.getMessage());
            return response;
        }

        boolean complete = authIdentity != null && authIdentity.getAuthCertificate() != null;
        boolean validClient = clientId != null && clientRegistry.isValidClient(clientId, redirectUri) != null;
        boolean authorized = complete;

        response.put("complete", complete);
        response.put("validClient", validClient);
        response.put("authorized", authorized);

        if (authorized && validClient && session != null) {
            OidcClient client = clientRegistry.getClient(clientId);
            if (client != null) {
                String code = java.util.UUID.randomUUID().toString();
                UserInfo user = new UserInfo(
                        session.getPersonalCode(),
                        authIdentity.getGivenName(),
                        authIdentity.getSurname(),
                        session.getCountry(),
                        authIdentity.getDateOfBirth().get(),
                        null,
                        nonce);

                String base64 = getCertificateBase64(authIdentity.getAuthCertificate());
                System.out.println("Base64-encoded certificate: " + base64); // Write to console output

                oidcSessionStore.storeCode(code, user);
                StringBuilder redirectUrl = new StringBuilder();
                redirectUrl.append(client.getRedirectUri()).append("?code=").append(code);
                if (state != null) {
                    redirectUrl.append("&state=").append(state);
                }
                response.put("redirectUrl", redirectUrl.toString());
            }
        }
        return response;
    }

    public static String getCertificateBase64(X509Certificate identity) {
        if (identity == null) {
            return null;
        }
        try {
            return Base64.getEncoder().encodeToString(identity.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode X509Certificate to Base64", e);
        }
    }
}
