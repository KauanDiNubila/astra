package com.astra.user.application.port.in;

import java.util.UUID;

public interface RedeemDesktopLoginCodeUseCase {

    UUID redeem(String code, String verifier);
}
