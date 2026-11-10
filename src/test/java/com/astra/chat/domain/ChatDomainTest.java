package com.astra.chat.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.astra.shared.exception.NotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatDomainTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-11-10T12:00:00Z");
    private static final UUID ANA = UUID.randomUUID();
    private static final UUID BETO = UUID.randomUUID();
    private static final UUID CAIO = UUID.randomUUID();
    private static final UUID GROUP = UUID.randomUUID();

    private static Message stored(UUID sender, Destination destination, String content) {
        return Message.withId(UUID.randomUUID(), sender, destination, content, null, NOW, null, null, null);
    }

    @Test
    void textoPrecisaDeConteudoEFotoAceitaLegendaVazia() {
        assertThatThrownBy(() -> Message.text(ANA, Destination.direct(BETO), "  "))
                .isInstanceOf(EmptyMessageException.class);
        assertThat(Message.photo(ANA, Destination.direct(BETO), " ").getContent()).isNull();
        assertThat(Message.photo(ANA, Destination.group(GROUP), "legenda").getContent()).isEqualTo("legenda");
        assertThatThrownBy(() -> new Destination(BETO, GROUP)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void respostaSoDentroDaMesmaConversa() {
        Message original = stored(BETO, Destination.direct(ANA), "pergunta");
        Message reply = Message.text(ANA, Destination.direct(BETO), "resposta");
        reply.replyTo(original);
        assertThat(reply.getReplyToMessageId()).isEqualTo(original.getId());

        Message toCaio = Message.text(ANA, Destination.direct(CAIO), "oi");
        assertThatThrownBy(() -> toCaio.replyTo(original)).isInstanceOf(ReplyOutsideConversationException.class);

        Message inGroup = stored(ANA, Destination.group(GROUP), "no grupo");
        assertThatThrownBy(() -> Message.text(ANA, Destination.direct(BETO), "x").replyTo(inGroup))
                .isInstanceOf(ReplyOutsideConversationException.class);
        assertThatThrownBy(() -> Message.text(ANA, Destination.group(GROUP), "x").replyTo(original))
                .isInstanceOf(ReplyOutsideConversationException.class);
        Message groupReply = Message.text(BETO, Destination.group(GROUP), "x");
        groupReply.replyTo(inGroup);
        assertThat(groupReply.getReplyToMessageId()).isEqualTo(inGroup.getId());
    }

    @Test
    void fixarRespeitaOLimiteEFixarDeNovoNaoMuda() {
        Message message = stored(ANA, Destination.direct(BETO), "importante");

        assertThatThrownBy(() -> message.pin(BETO, NOW, 3)).isInstanceOf(PinLimitReachedException.class);
        message.pin(BETO, NOW, 2);
        assertThat(message.getPinnedBy()).isEqualTo(BETO);
        message.pin(ANA, NOW.plusMinutes(1), 3);
        assertThat(message.getPinnedBy()).isEqualTo(BETO);
        message.unpin();
        assertThat(message.isPinned()).isFalse();
    }

    @Test
    void previaETextoDoResumo() {
        assertThat(stored(ANA, Destination.direct(BETO), null).preview()).isEqualTo("📷 Foto");
        assertThat(stored(ANA, Destination.direct(BETO), null).summaryText()).isEqualTo("📷 Foto");
        assertThat(stored(ANA, Destination.direct(BETO), "a".repeat(81)).preview()).isEqualTo("a".repeat(80) + "…");
        assertThat(stored(ANA, Destination.direct(BETO), "curta").preview()).isEqualTo("curta");
    }

    @Test
    void participantesDaConversaPrivada() {
        Message message = stored(ANA, Destination.direct(BETO), "oi");

        assertThat(message.involves(BETO)).isTrue();
        assertThat(message.involves(CAIO)).isFalse();
        assertThat(message.otherParticipant(BETO)).isEqualTo(ANA);
        assertThat(stored(ANA, Destination.group(GROUP), "oi").involves(BETO)).isFalse();
    }

    @Test
    void grupoNasceComCriadorEAmigosSemRepetirENomeAparado() {
        ChatGroup group = ChatGroup.create(" Estudo ", ANA, List.of(BETO, BETO, ANA), NOW);

        assertThat(group.getName()).isEqualTo("Estudo");
        assertThat(group.memberIds()).containsExactly(ANA, BETO);
        assertThat(group.wasCreatedBy(ANA)).isTrue();
        assertThat(group.getMembers().getFirst().lastReadAt()).isEqualTo(NOW);
    }

    @Test
    void soMembroAdicionaEQuemJaEstaNaoEntraDeNovo() {
        ChatGroup group = ChatGroup.create("Estudo", ANA, List.of(BETO), NOW);

        assertThatThrownBy(() -> group.addMember(CAIO, UUID.randomUUID(), NOW)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> group.addMember(BETO, ANA, NOW)).isInstanceOf(AlreadyGroupMemberException.class);
        group.addMember(BETO, CAIO, NOW);
        assertThat(group.isMember(CAIO)).isTrue();
    }

    @Test
    void imagemPrecisaSerImagemDeAte2MB() {
        assertThat(ChatImage.of("image/png", new byte[ChatImage.MAX_BYTES]).contentType()).isEqualTo("image/png");
        assertThatThrownBy(() -> ChatImage.of("text/plain", new byte[1])).hasMessage("Arquivo precisa ser uma imagem");
        assertThatThrownBy(() -> ChatImage.of("image/png", new byte[ChatImage.MAX_BYTES + 1]))
                .hasMessage("Imagem precisa ter até 2MB");
    }
}
