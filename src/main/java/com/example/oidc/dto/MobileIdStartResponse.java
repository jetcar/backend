package com.example.oidc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileIdStartResponse {
    public String sessionId;
    public String code;
}