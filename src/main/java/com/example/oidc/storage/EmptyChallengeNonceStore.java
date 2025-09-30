package com.example.oidc.storage;

import eu.webeid.security.challenge.ChallengeNonce;
import eu.webeid.security.challenge.ChallengeNonceStore;

import java.time.ZonedDateTime;

public class EmptyChallengeNonceStore implements ChallengeNonceStore {

    private static final long DEFAULT_EXPIRATION_SECONDS = 300;

    public EmptyChallengeNonceStore() {
        // No-op constructor
    }

    @Override
    public void put(ChallengeNonce challengeNonce) {
        // No-op
    }

    // Custom method to get and remove by nonce value (returns null)
    public ChallengeNonce getAndRemove(String nonce) {
        return null;
    }

    @Override
    public ChallengeNonce getAndRemoveImpl() {
        return null;
    }
}
