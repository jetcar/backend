package com.example.oidc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.id.ClientID;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;

@RestController
public class OidcAuthorizationController {
    private final OidcClientRegistry clientRegistry;

    @org.springframework.beans.factory.annotation.Autowired
    public OidcAuthorizationController(OidcClientRegistry clientRegistry) {
        this.clientRegistry = clientRegistry;
    }

    @GetMapping("/authorize")
    public org.springframework.web.servlet.view.RedirectView authorize(@RequestParam Map<String, String> params) {
        // Convert params to Map<String, List<String>>
        Map<String, java.util.List<String>> multiParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            multiParams.put(entry.getKey(), java.util.Collections.singletonList(entry.getValue()));
        }
        // Parse the OIDC authorization request
        AuthorizationRequest request;
        try {
            request = AuthorizationRequest.parse(URI.create("/authorize"), multiParams);
        } catch (com.nimbusds.oauth2.sdk.ParseException e) {
            return new org.springframework.web.servlet.view.RedirectView("/error?error=Invalid+authorization+request");
        }
        ClientID clientID = request.getClientID();
        // Validate client
        if (clientID == null || !clientRegistry.isValidClient(clientID.getValue())) {
            return new org.springframework.web.servlet.view.RedirectView("/error?error=Invalid+client_id");
        }
        // Redirect to index.html with all OIDC parameters
        StringBuilder redirectUrl = new StringBuilder("/index.html?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            redirectUrl.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        if (redirectUrl.charAt(redirectUrl.length() - 1) == '&') {
            redirectUrl.setLength(redirectUrl.length() - 1);
        }
        return new org.springframework.web.servlet.view.RedirectView(redirectUrl.toString());
    }
}
