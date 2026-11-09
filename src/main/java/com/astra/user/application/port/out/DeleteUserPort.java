package com.astra.user.application.port.out;

import java.util.UUID;

public interface DeleteUserPort {

    void delete(UUID userId);
}
