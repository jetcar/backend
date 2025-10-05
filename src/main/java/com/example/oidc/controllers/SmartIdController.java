package com.example.oidc.controllers;

import com.example.oidc.dto.SmartIdCheckResponse;
import com.example.oidc.dto.SmartIdStartResponse;
import com.example.oidc.service.SmartIdService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SmartIdController {

    private final SmartIdService smartIdService;

    @Autowired
    public SmartIdController(SmartIdService smartIdService) {
        this.smartIdService = smartIdService;
    }

    @PostMapping("/smartid/start")
    public SmartIdStartResponse startSmartId(
            @RequestParam String country,
            @RequestParam String personalCode) {
        return smartIdService.startSmartId(country, personalCode);
    }

    @GetMapping("/smartid/check")
    public SmartIdCheckResponse checkSmartId(
            @RequestParam String sessionId,
            @RequestParam(required = false) String client_id,
            @RequestParam(required = false) String redirect_uri,
            @RequestParam(required = false) String response_type,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String nonce) {
        return smartIdService.checkSmartId(sessionId, client_id, redirect_uri, response_type, scope, state, nonce);
    }
}
