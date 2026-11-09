package com.astra.social.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SendFriendRequestCommandTest {

    private static final UUID USER = UUID.randomUUID();

    @Test
    void separaNomeETagPeloUltimoJogoDaVelha() {
        SendFriendRequestCommand command = new SendFriendRequestCommand(USER, "Ana #1#0042");

        assertThat(command.name()).isEqualTo("Ana #1");
        assertThat(command.tag()).isEqualTo("0042");
    }

    @Test
    void recusaFormatoInvalido() {
        for (String handle : new String[] {null, "", "sem-tag", "nome#12", "nome#12345", "#0001", "nome#abcd"}) {
            assertThatThrownBy(() -> new SendFriendRequestCommand(USER, handle))
                    .as(handle)
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void recusaNomeAcimaDe120Caracteres() {
        assertThat(new SendFriendRequestCommand(USER, "a".repeat(120) + "#0001").name()).hasSize(120);
        assertThatThrownBy(() -> new SendFriendRequestCommand(USER, "a".repeat(121) + "#0001"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recusaUsuarioNulo() {
        assertThatThrownBy(() -> new SendFriendRequestCommand(null, "Ana#0001"))
                .isInstanceOf(NullPointerException.class);
    }
}
