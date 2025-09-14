package com.example.oidc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.oidc.OidcSessionStore.UserInfo;

public class InMemoryUserStore {
    private static final Map<String, UserInfo> tokenToUser = new ConcurrentHashMap<>();

    public static void storeUser(String accessToken, UserInfo user) {
        tokenToUser.put(accessToken, user);
    }

    public static UserInfo getUser(String accessToken) {
        return tokenToUser.get(accessToken);
    }
}


