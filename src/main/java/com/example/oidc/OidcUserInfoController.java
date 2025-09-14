package com.example.oidc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.oidc.OidcSessionStore.UserInfo;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;

@RestController
public class OidcUserInfoController {
    private final OidcSessionStore oidcSessionStore;

    @Autowired
    public OidcUserInfoController(OidcSessionStore oidcSessionStore) {
        this.oidcSessionStore = oidcSessionStore;
    }

    @GetMapping("/userinfo")
    public Map<String, Object> userinfo(@RequestParam(value = "access_token", required = false) String accessToken) {
        Map<String, Object> userInfo = new HashMap<>();
        if (accessToken == null || accessToken.isEmpty()) {
            userInfo.put("error", "Missing access_token");
            return userInfo;
        }
        UserInfo user = oidcSessionStore.getUserByToken(accessToken);
        if (user == null) {
            userInfo.put("error", "Invalid or expired access_token");
            return userInfo;
        }
        userInfo.put("sub", user.getSub());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmail());
        userInfo.put("country", user.getCountry());
        userInfo.put("phoneNumber", user.getPhoneNumber());
        return userInfo;
    }
}
