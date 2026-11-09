package com.astra.user.adapter.in.web;

import com.astra.shared.CurrentUserProvider;
import com.astra.shared.exception.ConflictException;
import com.astra.user.application.port.in.AcceptTermsUseCase;
import com.astra.user.application.port.in.ChangePasswordUseCase;
import com.astra.user.application.port.in.DeleteOwnAccountUseCase;
import com.astra.user.application.port.in.GetAvatarQuery;
import com.astra.user.application.port.in.GetUserQuery;
import com.astra.user.application.port.in.UpdateAvatarCommand;
import com.astra.user.application.port.in.UpdateAvatarUseCase;
import com.astra.user.application.port.in.UpdateProfileUseCase;
import com.astra.user.application.port.in.UserResponse;
import com.astra.user.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
class UserController {

    private final GetUserQuery getUserQuery;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final AcceptTermsUseCase acceptTermsUseCase;
    private final DeleteOwnAccountUseCase deleteOwnAccountUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final UpdateAvatarUseCase updateAvatarUseCase;
    private final GetAvatarQuery getAvatarQuery;
    private final RefreshTokenService refreshTokenService;
    private final CurrentUserProvider currentUserProvider;

    UserController(GetUserQuery getUserQuery, UpdateProfileUseCase updateProfileUseCase,
                   AcceptTermsUseCase acceptTermsUseCase, DeleteOwnAccountUseCase deleteOwnAccountUseCase,
                   ChangePasswordUseCase changePasswordUseCase, UpdateAvatarUseCase updateAvatarUseCase,
                   GetAvatarQuery getAvatarQuery, RefreshTokenService refreshTokenService,
                   CurrentUserProvider currentUserProvider) {
        this.getUserQuery = getUserQuery;
        this.updateProfileUseCase = updateProfileUseCase;
        this.acceptTermsUseCase = acceptTermsUseCase;
        this.deleteOwnAccountUseCase = deleteOwnAccountUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.updateAvatarUseCase = updateAvatarUseCase;
        this.getAvatarQuery = getAvatarQuery;
        this.refreshTokenService = refreshTokenService;
        this.currentUserProvider = currentUserProvider;
    }

    private UUID currentUser() {
        return currentUserProvider.currentUserId();
    }

    @GetMapping("/me")
    UserResponse me(HttpServletResponse ignored) {
        return getUserQuery.get(currentUser());
    }

    @PutMapping("/me")
    UserResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return updateProfileUseCase.update(request.toCommand(currentUser()));
    }

    @PostMapping("/me/terms")
    UserResponse acceptTerms() {
        return acceptTermsUseCase.accept(currentUser());
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAccount(@Valid @RequestBody DeleteAccountRequest request, HttpServletResponse response) {
        deleteOwnAccountUseCase.delete(request.toCommand(currentUser()));
        refreshTokenService.clearCookie(response);
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        changePasswordUseCase.change(request.toCommand(currentUser()));
    }

    @PostMapping("/me/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateAvatar(@RequestParam("file") MultipartFile file) {
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new ConflictException("Não foi possível ler o arquivo");
        }
        updateAvatarUseCase.update(new UpdateAvatarCommand(currentUser(), file.getContentType(), bytes));
    }

    @GetMapping("/users/{id}/avatar")
    ResponseEntity<byte[]> avatar(@PathVariable UUID id) {
        return getAvatarQuery.avatar(id)
                .map(avatar -> ResponseEntity.ok()
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePrivate())
                        .contentType(MediaType.parseMediaType(avatar.contentType()))
                        .body(avatar.bytes()))
                .orElseGet(() -> ResponseEntity.noContent()
                        .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePrivate())
                        .build());
    }
}
