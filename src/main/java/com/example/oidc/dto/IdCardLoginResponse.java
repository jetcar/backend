package com.example.oidc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdCardLoginResponse {
    public String error;
    public String message;
    public String redirectUrl;
    // Add more fields if needed
}