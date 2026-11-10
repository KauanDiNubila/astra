package com.astra.user.application.port.out;

import com.astra.user.domain.DesktopLoginCode;
import java.util.Optional;

public interface DesktopLoginCodeStorePort {

    void save(String code, DesktopLoginCode loginCode);

    Optional<DesktopLoginCode> take(String code);
}
