package com.astra.roadmap.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoadmapCommandsTest {

    private static final UUID USER = UUID.randomUUID();
    private static final UUID ID = UUID.randomUUID();

    @Test
    void roadmapEPassoPrecisamDeTituloDentroDoLimite() {
        assertThat(new CreateRoadmapCommand(USER, "a".repeat(160), "s".repeat(200)).title()).hasSize(160);
        assertThatThrownBy(() -> new CreateRoadmapCommand(USER, " ", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CreateRoadmapCommand(USER, "X", "s".repeat(201)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AddStepCommand(USER, ID, "X", -1, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AddStepCommand(USER, ID, "X", 1, null, "d".repeat(2001)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void linkPrecisaDeRotuloEUrl() {
        assertThatThrownBy(() -> new AddStepResourceCommand(USER, ID, "X", " "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AddStepResourceCommand(USER, ID, null, "https://x"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AddStepResourceCommand(USER, ID, "X", "u".repeat(501)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pinAceitaNotaDeUmACincoOuNenhuma() {
        assertThat(new PinCourseCommand(USER, ID, ID, null, null).rating()).isNull();
        assertThat(new PinCourseCommand(USER, ID, ID, "x".repeat(30), 5).rating()).isEqualTo(5);
        assertThatThrownBy(() -> new PinCourseCommand(USER, ID, ID, null, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PinCourseCommand(USER, ID, ID, null, 6))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PinCourseCommand(USER, ID, ID, "x".repeat(31), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PinCourseCommand(USER, ID, null, null, null))
                .isInstanceOf(NullPointerException.class);
    }
}
