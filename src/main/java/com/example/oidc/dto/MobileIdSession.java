package com.example.oidc.dto;

public class MobileIdSession {
    private boolean complete;
    private String country;
    private String personalCode;
    private String phoneNumber;
    private String authenticationHash;

    public MobileIdSession() {
        // Default constructor
    }

    public MobileIdSession(boolean complete, String country, String personalCode, String phoneNumber,
            String authenticationHash) {
        this.complete = complete;
        this.country = country;
        this.personalCode = personalCode;
        this.phoneNumber = phoneNumber;
        this.authenticationHash = authenticationHash;
    }

    public boolean isComplete() {
        return complete;
    }

    public String getCountry() {
        return country;
    }

    public String getPersonalCode() {
        return personalCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAuthenticationHash() {
        return authenticationHash;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setPersonalCode(String personalCode) {
        this.personalCode = personalCode;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAuthenticationHash(String authenticationHash) {
        this.authenticationHash = authenticationHash;
    }
}