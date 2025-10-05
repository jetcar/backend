package com.example.oidc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmartIdCheckResponse {
    public String sessionId;
    public Boolean complete;
    public Boolean validClient;
    public Boolean authorized;
    public String error;
    public String redirectUrl;
}