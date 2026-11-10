package com.astra.user.application.service;

import java.security.SecureRandom;
import java.util.Base64;

final class RandomTokens {

    private static final SecureRandom RANDOM = new SecureRandom();

    private RandomTokens() {
    }

    static String next() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
