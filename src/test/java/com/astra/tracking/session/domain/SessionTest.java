package com.astra.tracking.session.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SessionTest {

    private static final UUID USER = UUID.randomUUID();
    private static final UUID CATEGORY = UUID.randomUUID();
    private static final OffsetDateTime STARTED_AT = OffsetDateTime.parse("2026-08-03T14:00:00-03:00");

    private Session session(int minutes) {
        return Session.start(USER, CATEGORY, null, minutes, STARTED_AT, null);
    }

    @Test
    void comecaSemIdESemRepositorio() {
        Session session = session(50);

        assertThat(session.getId()).isNull();
        assertThat(session.getGithubRepositoryId()).isNull();
        assertThat(session.getFocusedMinutes()).isEqualTo(50);
    }

    @Test
    void naoNasceComMinutosZeroOuNegativos() {
        assertThatThrownBy(() -> session(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> session(-5)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void naoNasceSemUsuarioCategoriaOuInicio() {
        assertThatThrownBy(() -> Session.start(null, CATEGORY, null, 30, STARTED_AT, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Session.start(USER, null, null, 30, STARTED_AT, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Session.start(USER, CATEGORY, null, 30, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void sessaoCarregadaPrecisaDeId() {
        assertThatThrownBy(() -> Session.withId(null, USER, CATEGORY, null, null, 30, STARTED_AT, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void sabeQuemEDono() {
        Session session = session(30);

        assertThat(session.isOwnedBy(USER)).isTrue();
        assertThat(session.isOwnedBy(UUID.randomUUID())).isFalse();
    }

    @Test
    void terminaNoInicioMaisOsMinutosFocados() {
        assertThat(session(90).endedAt()).isEqualTo(OffsetDateTime.parse("2026-08-03T15:30:00-03:00"));
    }

    @Test
    void vinculaEDesvinculaRepositorio() {
        Session session = session(30);
        UUID repository = UUID.randomUUID();

        session.linkRepository(repository);
        assertThat(session.getGithubRepositoryId()).isEqualTo(repository);

        session.unlinkRepository();
        assertThat(session.getGithubRepositoryId()).isNull();
    }

    @Test
    void naoVinculaRepositorioNulo() {
        assertThatThrownBy(() -> session(30).linkRepository(null)).isInstanceOf(NullPointerException.class);
    }
}
