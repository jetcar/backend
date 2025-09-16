package com.example.oidc.dto;

public class MobileIdSession {
    public boolean complete;
    public String country;
    public String personalCode;
    public String phoneNumber;
    public String authenticationHash; // renamed and added

    public MobileIdSession() {
    }

    public MobileIdSession(boolean complete, String country, String personalCode, String phoneNumber,
            String authenticationHash) {
        this.complete = complete;
        this.country = country;
        this.personalCode = personalCode;
        this.phoneNumber = phoneNumber;
        this.authenticationHash = authenticationHash;
    }
}