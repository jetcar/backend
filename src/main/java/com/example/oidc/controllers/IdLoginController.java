package com.example.oidc.controllers;

import com.example.oidc.dto.IdCardChallengeResponse;
import com.example.oidc.dto.IdCardLoginRequest;
import com.example.oidc.dto.IdCardLoginResponse;
import com.example.oidc.service.IdcardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import java.security.cert.CertificateEncodingException;

@RestController
@RequestMapping("/idlogin")
public class IdLoginController {

    private final IdcardService idcardService;

    @Autowired
    public IdLoginController(IdcardService idcardService) {
        this.idcardService = idcardService;
    }

    @GetMapping("/challenge")
    public IdCardChallengeResponse challenge(
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "nonce", required = false) String nonce) {
        return idcardService.createChallenge(clientId, redirectUri, state, nonce);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public IdCardLoginResponse login(
            @RequestBody IdCardLoginRequest body,
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "nonce", required = false) String nonce,
            @RequestParam(value = "sessionId", required = false) String sessionId) throws CertificateEncodingException {
        return idcardService.login(body, clientId, redirectUri, state, nonce, sessionId);
    }
}
