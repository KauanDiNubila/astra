package com.astra.github.application.port.out;

import com.astra.github.domain.TokenGrant;

public interface ExchangeOAuthCodePort {

    TokenGrant exchangeCode(String code);
}
