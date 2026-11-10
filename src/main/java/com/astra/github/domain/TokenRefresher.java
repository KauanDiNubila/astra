package com.astra.github.domain;

public interface TokenRefresher {

    TokenGrant refresh(String refreshToken);
}
