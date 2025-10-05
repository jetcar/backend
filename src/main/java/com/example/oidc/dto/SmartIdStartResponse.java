package com.example.oidc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmartIdStartResponse {
    public String sessionId;
    public String code;
    public String country;
}