package com.astra.tracking.category.application.port.in;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateCategoryCommandTest {

    private static final UUID USER = UUID.randomUUID();

    @Test
    void aceitaNomeECorNosLimitesECorNula() {
        assertThatCode(() -> new CreateCategoryCommand(USER, "a".repeat(80), "#".repeat(20)))
                .doesNotThrowAnyException();
        assertThatCode(() -> new CreateCategoryCommand(USER, "Estudo", null)).doesNotThrowAnyException();
    }

    @Test
    void recusaNomeVazioEmBrancoOuLongo() {
        assertThatThrownBy(() -> new CreateCategoryCommand(USER, null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CreateCategoryCommand(USER, "  ", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CreateCategoryCommand(USER, "a".repeat(81), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recusaCorLongaEUsuarioNulo() {
        assertThatThrownBy(() -> new CreateCategoryCommand(USER, "Estudo", "#".repeat(21)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CreateCategoryCommand(null, "Estudo", null))
                .isInstanceOf(NullPointerException.class);
    }
}
