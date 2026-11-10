package com.astra.chat.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.astra.chat.application.port.in.ImageUpload;
import com.astra.chat.application.port.in.PinChange;
import com.astra.chat.application.port.in.SendDirectMessageCommand;
import com.astra.chat.application.port.out.CheckFriendshipPort;
import com.astra.chat.application.port.out.CheckGroupMemberPort;
import com.astra.chat.application.port.out.CreateMessagePort;
import com.astra.chat.application.port.out.ListMessagesPort;
import com.astra.chat.application.port.out.LoadAttachmentPort;
import com.astra.chat.application.port.out.LoadMessagePort;
import com.astra.chat.application.port.out.UpdateMessagePinPort;
import com.astra.chat.domain.Destination;
import com.astra.chat.domain.EmptyMessageException;
import com.astra.chat.domain.InvalidChatImageException;
import com.astra.chat.domain.Message;
import com.astra.shared.exception.NotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatServicesTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-10T12:00:00Z"), ZoneOffset.UTC);
    private static final OffsetDateTime NOW = OffsetDateTime.now(CLOCK);
    private static final UUID ANA = UUID.randomUUID();
    private static final UUID BETO = UUID.randomUUID();
    private static final UUID CAIO = UUID.randomUUID();

    private final CheckFriendshipPort friendship = mock(CheckFriendshipPort.class);
    private final CheckGroupMemberPort membership = mock(CheckGroupMemberPort.class);
    private final LoadMessagePort loadMessagePort = mock(LoadMessagePort.class);
    private final CreateMessagePort createMessagePort = mock(CreateMessagePort.class);
    private final ListMessagesPort listMessagesPort = mock(ListMessagesPort.class);
    private final UpdateMessagePinPort updatePinPort = mock(UpdateMessagePinPort.class);
    private final MessageResponses responses = new MessageResponses(mock(LoadAttachmentPort.class), loadMessagePort);

    private final SendDirectMessageService send =
            new SendDirectMessageService(friendship, loadMessagePort, createMessagePort, responses);
    private final PinMessageService pin = new PinMessageService(loadMessagePort, listMessagesPort, updatePinPort,
            friendship, membership, responses, CLOCK);

    private static Message stored(UUID sender, Destination destination) {
        return Message.withId(UUID.randomUUID(), sender, destination, "oi", null, NOW, null, null, null);
    }

    @Test
    void textoEmBrancoRecusaAntesDeConsultarAmizade() {
        assertThatThrownBy(() -> send.send(new SendDirectMessageCommand(ANA, BETO, " ", null, null)))
                .isInstanceOf(EmptyMessageException.class);
        verifyNoInteractions(friendship, createMessagePort);
    }

    @Test
    void fotoConfereAmizadeAntesDoArquivo() {
        SendDirectMessageCommand command = new SendDirectMessageCommand(ANA, BETO, null, null,
                new ImageUpload("text/plain", new byte[1]));

        assertThatThrownBy(() -> send.send(command)).isInstanceOf(NotFoundException.class);
        when(friendship.areFriends(ANA, BETO)).thenReturn(true);
        assertThatThrownBy(() -> send.send(command)).isInstanceOf(InvalidChatImageException.class);
        verify(createMessagePort, never()).create(any(), any());
    }

    @Test
    void fixarConversaPrivadaAvisaOsDoisEContaAsFixadas() {
        Message message = stored(ANA, Destination.direct(BETO));
        when(loadMessagePort.load(message.getId())).thenReturn(Optional.of(message));
        when(friendship.areFriends(BETO, ANA)).thenReturn(true);
        when(listMessagesPort.pinnedDirect(BETO, ANA)).thenReturn(List.of());

        PinChange change = pin.setPinned(BETO, message.getId(), true);

        assertThat(change.audience()).containsExactly(ANA, BETO);
        assertThat(change.message().pinnedBy()).isEqualTo(BETO);
        verify(updatePinPort).updatePin(message);
    }

    @Test
    void quemNaoEstaNaConversaNemSabeQueAMensagemExiste() {
        Message message = stored(ANA, Destination.direct(BETO));
        when(loadMessagePort.load(message.getId())).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> pin.setPinned(CAIO, message.getId(), true))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Mensagem não encontrada");
        verify(updatePinPort, never()).updatePin(any());
    }
}
