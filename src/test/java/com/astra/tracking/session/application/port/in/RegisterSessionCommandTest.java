package com.astra.tracking.session.application.port.in;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegisterSessionCommandTest {

    private static final UUID USER = UUID.randomUUID();
    private static final UUID CATEGORY = UUID.randomUUID();
    private static final OffsetDateTime STARTED_AT = OffsetDateTime.parse("2026-08-03T14:00:00-03:00");

    @Test
    void aceitaComandoValidoSemCursoESemNota() {
        assertThatCode(() -> new RegisterSessionCommand(USER, CATEGORY, null, 30, STARTED_AT, null))
                .doesNotThrowAnyException();
    }

    @Test
    void recusaMinutosZeroOuNegativos() {
        assertThatThrownBy(() -> new RegisterSessionCommand(USER, CATEGORY, null, 0, STARTED_AT, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RegisterSessionCommand(USER, CATEGORY, null, -5, STARTED_AT, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aceitaNotaNoLimiteERecusaAcima() {
        String limit = "a".repeat(RegisterSessionCommand.NOTE_MAX_LENGTH);

        assertThatCode(() -> new RegisterSessionCommand(USER, CATEGORY, null, 30, STARTED_AT, limit))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> new RegisterSessionCommand(USER, CATEGORY, null, 30, STARTED_AT, limit + "a"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recusaCamposObrigatoriosNulos() {
        assertThatThrownBy(() -> new RegisterSessionCommand(null, CATEGORY, null, 30, STARTED_AT, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RegisterSessionCommand(USER, null, null, 30, STARTED_AT, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RegisterSessionCommand(USER, CATEGORY, null, 30, null, null))
                .isInstanceOf(NullPointerException.class);
    }
}
