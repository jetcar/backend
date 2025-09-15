package com.example.oidc.dto;

public class SmartIdSession {
    public boolean complete;
    public String country;
    public String personalCode;

    public SmartIdSession() {}

    public SmartIdSession(boolean complete, String country, String personalCode) {
        this.complete = complete;
        this.country = country;
        this.personalCode = personalCode;
    }
}