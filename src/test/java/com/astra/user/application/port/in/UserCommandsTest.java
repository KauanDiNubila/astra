package com.astra.user.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserCommandsTest {

    private static final UUID USER = UUID.randomUUID();
    private static final String PASSWORD = "Xk9$mQ2vN8pL4wR7";

    @Test
    void cadastroValidaNomeEmailESenha() {
        assertThat(new RegisterUserCommand("a".repeat(120), "a@astra.local", "p".repeat(8), false).name())
                .hasSize(120);
        for (String name : new String[] {null, " ", "a".repeat(121), "Com#"}) {
            assertThatThrownBy(() -> new RegisterUserCommand(name, "a@astra.local", PASSWORD, false))
                    .as(String.valueOf(name))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> new RegisterUserCommand("Ok", "e".repeat(181), PASSWORD, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RegisterUserCommand("Ok", "a@astra.local", "curta", false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RegisterUserCommand("Ok", "a@astra.local", "p".repeat(101), false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void perfilESenhaNovaSeguemAsMesmasRegras() {
        assertThat(new UpdateProfileCommand(USER, "Ok", null).bio()).isNull();
        assertThatThrownBy(() -> new UpdateProfileCommand(USER, "Com#", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new UpdateProfileCommand(USER, "Ok", "b".repeat(81)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ChangePasswordCommand(USER, PASSWORD, "curta"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new ChangePasswordCommand(USER, "qualquer", PASSWORD).newPassword()).isEqualTo(PASSWORD);
    }

    @Test
    void resumoMontaOHandleComATag() {
        assertThat(new UserSummary(USER, "Ana", "0042", null, false).handle()).isEqualTo("Ana#0042");
    }
}
