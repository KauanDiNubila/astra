package com.astra.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.astra.shared.exception.ConflictException;
import com.astra.shared.exception.UnauthorizedException;
import com.astra.user.application.port.in.ChangePasswordCommand;
import com.astra.user.application.port.in.LogInCommand;
import com.astra.user.application.port.in.OAuthLogInCommand;
import com.astra.user.application.port.in.RegisterUserCommand;
import com.astra.user.application.port.out.CheckPasswordBreachPort;
import com.astra.user.application.port.out.CreateUserPort;
import com.astra.user.application.port.out.HandlePort;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.application.port.out.OAuthConnectionPort;
import com.astra.user.application.port.out.PasswordHasherPort;
import com.astra.user.application.port.out.PublishUserRegisteredPort;
import com.astra.user.application.port.out.UpdateUserPort;
import com.astra.user.domain.Role;
import com.astra.user.domain.User;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserServicesTest {

    private static final String PASSWORD = "Xk9$mQ2vN8pL4wR7";
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-09T12:00:00Z"), ZoneOffset.UTC);

    private final LoadUserPort loadUserPort = mock(LoadUserPort.class);
    private final CreateUserPort createUserPort = mock(CreateUserPort.class);
    private final UpdateUserPort updateUserPort = mock(UpdateUserPort.class);
    private final PasswordHasherPort hasher = mock(PasswordHasherPort.class);
    private final CheckPasswordBreachPort breach = mock(CheckPasswordBreachPort.class);
    private final PublishUserRegisteredPort publisher = mock(PublishUserRegisteredPort.class);
    private final OAuthConnectionPort connections = mock(OAuthConnectionPort.class);
    private final TagGenerator tags = new TagGenerator(mock(HandlePort.class));

    private User stored(String passwordHash) {
        User user = User.withId(UUID.randomUUID(), "Ana", "ana@astra.local", passwordHash, null, Role.USER, null,
                "0001", null, null, OffsetDateTime.now(CLOCK));
        when(loadUserPort.load(user.getId())).thenReturn(Optional.of(user));
        when(loadUserPort.loadByEmail(user.getEmail())).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void cadastroComSenhaVazadaOuEmailRepetidoNaoGrava() {
        RegisterUserService service = new RegisterUserService(breach, loadUserPort, createUserPort, hasher, publisher,
                tags, CLOCK);
        when(breach.isBreached(PASSWORD)).thenReturn(true);
        assertThatThrownBy(() -> service.register(new RegisterUserCommand("Ana", "x@astra.local", PASSWORD, false)))
                .isInstanceOf(ConflictException.class);

        stored("hash");
        when(breach.isBreached(PASSWORD)).thenReturn(false);
        assertThatThrownBy(() -> service.register(new RegisterUserCommand("Ana", "ana@astra.local", PASSWORD, true)))
                .hasMessage("Email already registered");
        verify(createUserPort, never()).create(any());
        verify(publisher, never()).publish(any());
    }

    @Test
    void cadastroGravaComSenhaCifradaTermosEAvisaQueNasceu() {
        when(hasher.hash(PASSWORD)).thenReturn("cifrada");
        UUID id = UUID.randomUUID();
        when(createUserPort.create(any())).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return User.withId(id, u.getName(), u.getEmail(), u.getPasswordHash(), null, u.getRole(), null,
                    u.getTag(), u.getTermsAcceptedAt(), u.getTermsVersion(), OffsetDateTime.now(CLOCK));
        });

        var response = new RegisterUserService(breach, loadUserPort, createUserPort, hasher, publisher, tags, CLOCK)
                .register(new RegisterUserCommand("Ana", "nova@astra.local", PASSWORD, true));

        assertThat(response.termsAccepted()).isTrue();
        assertThat(response.hasPassword()).isTrue();
        verify(publisher).publish(id);
    }

    @Test
    void loginRecusaSenhaErradaEContaSemSenha() {
        LogInService service = new LogInService(loadUserPort, hasher);
        User user = stored("hash");
        when(hasher.matches(PASSWORD, "hash")).thenReturn(true);

        assertThat(service.logIn(new LogInCommand(user.getEmail(), PASSWORD)).id()).isEqualTo(user.getId());
        assertThatThrownBy(() -> service.logIn(new LogInCommand(user.getEmail(), "errada")))
                .isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> service.logIn(new LogInCommand("ninguem@astra.local", PASSWORD)))
                .isInstanceOf(UnauthorizedException.class);

        User noPassword = User.withId(UUID.randomUUID(), "G", "g@astra.local", null, null, Role.USER, null, "0002",
                null, null, null);
        when(loadUserPort.loadByEmail("g@astra.local")).thenReturn(Optional.of(noPassword));
        assertThatThrownBy(() -> service.logIn(new LogInCommand("g@astra.local", PASSWORD)))
                .isInstanceOf(UnauthorizedException.class);
        verify(hasher, never()).matches(anyString(), org.mockito.ArgumentMatchers.isNull());
    }

    @Test
    void oauthComConexaoExistenteNaoCriaNada() {
        UUID existing = UUID.randomUUID();
        when(connections.findUserId("google", "sub")).thenReturn(Optional.of(existing));

        UUID id = new OAuthLogInService(connections, loadUserPort, createUserPort, publisher, tags, CLOCK)
                .logIn(new OAuthLogInCommand("google", "sub", "a@astra.local", "A"));

        assertThat(id).isEqualTo(existing);
        verify(createUserPort, never()).create(any());
        verify(connections, never()).create(any());
    }

    @Test
    void trocarSenhaConfereAAtualAntesDeCifrarANova() {
        ChangePasswordService service = new ChangePasswordService(loadUserPort, updateUserPort, hasher, breach);
        User user = stored("hash");

        assertThatThrownBy(() -> service.change(new ChangePasswordCommand(user.getId(), "errada", PASSWORD)))
                .hasMessage("Senha atual incorreta");
        verify(hasher, never()).hash(any());

        when(hasher.matches("certa", "hash")).thenReturn(true);
        when(hasher.hash(PASSWORD)).thenReturn("nova");
        service.change(new ChangePasswordCommand(user.getId(), "certa", PASSWORD));
        assertThat(user.getPasswordHash()).isEqualTo("nova");
        verify(updateUserPort).update(user);
    }
}
