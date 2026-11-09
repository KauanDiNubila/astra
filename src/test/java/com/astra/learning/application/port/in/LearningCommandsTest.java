package com.astra.learning.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.astra.learning.domain.GoalType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LearningCommandsTest {

    private static final UUID USER = UUID.randomUUID();
    private static final UUID COURSE = UUID.randomUUID();
    private static final UUID MODULE = UUID.randomUUID();

    @Test
    void cursoPrecisaDeTituloEContagemNaoNegativa() {
        assertThat(new CreateCourseCommand(USER, "a".repeat(160), "p".repeat(80), 0).moduleCount()).isZero();
        for (String title : new String[] {null, "", "  ", "a".repeat(161)}) {
            assertThatThrownBy(() -> new CreateCourseCommand(USER, title, null, 0))
                    .as(String.valueOf(title))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> new CreateCourseCommand(USER, "X", "p".repeat(81), 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CreateCourseCommand(USER, "X", null, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CreateCourseCommand(null, "X", null, 0))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void moduloEAulaRecusamPosicaoNegativaETituloVazio() {
        assertThatThrownBy(() -> new AddModuleCommand(USER, COURSE, "X", -1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AddModuleCommand(USER, COURSE, "X", 1, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AddLessonCommand(USER, COURSE, MODULE, " ", 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AddLessonCommand(USER, COURSE, MODULE, "X", -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void editarModuloAceitaCamposAusentesMasLimitaOTitulo() {
        assertThat(new UpdateModuleCommand(USER, COURSE, MODULE, null, null).title()).isNull();
        assertThat(new UpdateModuleCommand(USER, COURSE, MODULE, "  ", true).title()).isEqualTo("  ");
        assertThatThrownBy(() -> new UpdateModuleCommand(USER, COURSE, MODULE, "a".repeat(161), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void metaPrecisaDeTipoEHorasPositivas() {
        assertThatThrownBy(() -> new SetGoalCommand(USER, GoalType.DAILY, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SetGoalCommand(USER, null, 1)).isInstanceOf(NullPointerException.class);
    }
}
