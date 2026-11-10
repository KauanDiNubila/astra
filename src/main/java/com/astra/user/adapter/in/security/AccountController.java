package com.astra.user.adapter.in.security;

import com.astra.shared.CurrentUserProvider;
import com.astra.user.application.port.in.DeleteOwnAccountUseCase;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AccountController {

    private final DeleteOwnAccountUseCase deleteOwnAccountUseCase;
    private final RefreshTokenCookie refreshTokenCookie;
    private final CurrentUserProvider currentUserProvider;

    AccountController(DeleteOwnAccountUseCase deleteOwnAccountUseCase, RefreshTokenCookie refreshTokenCookie,
                      CurrentUserProvider currentUserProvider) {
        this.deleteOwnAccountUseCase = deleteOwnAccountUseCase;
        this.refreshTokenCookie = refreshTokenCookie;
        this.currentUserProvider = currentUserProvider;
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAccount(@Valid @RequestBody DeleteAccountRequest request, HttpServletResponse response) {
        deleteOwnAccountUseCase.delete(request.toCommand(currentUserProvider.currentUserId()));
        refreshTokenCookie.clear(response);
    }
}
