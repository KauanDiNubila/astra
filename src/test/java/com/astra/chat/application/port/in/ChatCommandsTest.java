package com.astra.chat.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatCommandsTest {

    private static final UUID USER = UUID.randomUUID();
    private static final UUID OTHER = UUID.randomUUID();

    @Test
    void mensagemTemNoMaximo2000Caracteres() {
        assertThat(new SendDirectMessageCommand(USER, OTHER, "a".repeat(2000), null, null).content()).hasSize(2000);
        assertThatThrownBy(() -> new SendDirectMessageCommand(USER, OTHER, "a".repeat(2001), null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SendGroupMessageCommand(USER, OTHER, "a".repeat(2001), null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new SendGroupMessageCommand(USER, OTHER, null, null, new ImageUpload("image/png", new byte[1]))
                .content()).isNull();
    }

    @Test
    void grupoPrecisaDeNomeEDeMembros() {
        List<UUID> members = new ArrayList<>(List.of(OTHER));
        CreateGroupCommand command = new CreateGroupCommand(USER, "a".repeat(60), members);
        members.add(UUID.randomUUID());

        assertThat(command.memberIds()).containsExactly(OTHER);
        assertThatThrownBy(() -> new CreateGroupCommand(USER, " ", List.of(OTHER)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CreateGroupCommand(USER, "a".repeat(61), List.of(OTHER)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CreateGroupCommand(USER, "Estudo", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
