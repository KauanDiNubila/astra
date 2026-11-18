package com.astra.user.application.service;

import com.astra.shared.exception.ConflictException;
import com.astra.user.application.port.in.RequestPasswordResetUseCase;
import com.astra.user.application.port.in.ResetPasswordCommand;
import com.astra.user.application.port.in.ResetPasswordUseCase;
import com.astra.user.application.port.out.CheckPasswordBreachPort;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.application.port.out.PasswordHasherPort;
import com.astra.user.application.port.out.PasswordResetTokenPort;
import com.astra.user.application.port.out.RevokeAllRefreshTokensPort;
import com.astra.user.application.port.out.SendPasswordResetEmailPort;
import com.astra.user.application.port.out.UpdateUserPort;
import com.astra.user.domain.PasswordResetToken;
import com.astra.user.domain.RefreshToken;
import com.astra.user.domain.User;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PasswordResetService implements RequestPasswordResetUseCase, ResetPasswordUseCase {

    private final LoadUserPort loadUserPort;
    private final UpdateUserPort updateUserPort;
    private final PasswordHasherPort passwordHasherPort;
    private final CheckPasswordBreachPort checkPasswordBreachPort;
    private final PasswordResetTokenPort passwordResetTokenPort;
    private final SendPasswordResetEmailPort sendPasswordResetEmailPort;
    private final RevokeAllRefreshTokensPort revokeAllRefreshTokensPort;
    private final Clock clock;

    PasswordResetService(LoadUserPort loadUserPort, UpdateUserPort updateUserPort,
                         PasswordHasherPort passwordHasherPort, CheckPasswordBreachPort checkPasswordBreachPort,
                         PasswordResetTokenPort passwordResetTokenPort,
                         SendPasswordResetEmailPort sendPasswordResetEmailPort,
                         RevokeAllRefreshTokensPort revokeAllRefreshTokensPort, Clock clock) {
        this.loadUserPort = loadUserPort;
        this.updateUserPort = updateUserPort;
        this.passwordHasherPort = passwordHasherPort;
        this.checkPasswordBreachPort = checkPasswordBreachPort;
        this.passwordResetTokenPort = passwordResetTokenPort;
        this.sendPasswordResetEmailPort = sendPasswordResetEmailPort;
        this.revokeAllRefreshTokensPort = revokeAllRefreshTokensPort;
        this.clock = clock;
    }

    @Override
    public void request(String email) {
        Optional<User> user = loadUserPort.loadByEmail(email);
        if (user.isEmpty()) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now(clock);
        boolean justSent = passwordResetTokenPort.loadLatestFor(user.get().getId())
                .map(token -> token.isWithinResendCooldown(now))
                .orElse(false);
        if (justSent) {
            return;
        }
        String rawToken = RandomTokens.next();
        passwordResetTokenPort.create(PasswordResetToken.issue(user.get().getId(), rawToken, now));
        sendPasswordResetEmailPort.sendPasswordReset(user.get().getEmail(), rawToken);
    }

    @Override
    @Transactional
    public void reset(ResetPasswordCommand command) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        PasswordResetToken token = passwordResetTokenPort.loadByHash(RefreshToken.hash(command.token()))
                .orElseThrow(() -> new ConflictException(PasswordResetToken.INVALID_LINK));
        token.requireUsable(now);
        if (checkPasswordBreachPort.isBreached(command.newPassword())) {
            throw new ConflictException("Essa senha já apareceu em vazamentos conhecidos - escolha outra");
        }
        if (passwordResetTokenPort.invalidateAllFor(token.getUserId(), now) == 0) {
            throw new ConflictException(PasswordResetToken.INVALID_LINK);
        }
        User user = OwnedUser.target(loadUserPort, token.getUserId());
        user.resetPasswordHash(passwordHasherPort.hash(command.newPassword()));
        updateUserPort.update(user);
        revokeAllRefreshTokensPort.revokeAllFor(user.getId(), now);
    }
}
