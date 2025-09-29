package com.example.oidc.controllers;

import com.example.oidc.service.MobileIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MobileIdController {

    private final MobileIdService mobileIdService;

    @Autowired
    public MobileIdController(MobileIdService mobileIdService) {
        this.mobileIdService = mobileIdService;
    }

    @PostMapping("/mobileid/start")
    public ResponseEntity<?> startMobileId(
            @RequestParam String country,
            @RequestParam String personalCode,
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String client_id,
            @RequestParam(required = false) String redirect_uri) {
        return ResponseEntity.ok(
                mobileIdService.startMobileId(country, personalCode, phoneNumber, client_id, redirect_uri));
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
        return ResponseEntity.ok(
                mobileIdService.checkMobileId(sessionId, client_id, redirect_uri, response_type, scope, state, nonce));
    }
}
