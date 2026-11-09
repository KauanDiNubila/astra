package com.astra.social.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.astra.shared.exception.NotFoundException;
import com.astra.social.application.port.in.ExportFriendshipsQuery.FriendshipExport;
import com.astra.social.application.port.in.FriendshipResponse;
import com.astra.social.application.port.in.SendFriendRequestCommand;
import com.astra.social.application.port.out.CreateFriendshipPort;
import com.astra.social.application.port.out.DeleteFriendshipPort;
import com.astra.social.application.port.out.FindUserByHandlePort;
import com.astra.social.application.port.out.ListFriendshipsPort;
import com.astra.social.application.port.out.LoadFriendshipPort;
import com.astra.social.application.port.out.LoadUserSummariesPort;
import com.astra.social.application.port.out.LoadUserSummariesPort.UserSummary;
import com.astra.social.application.port.out.UpdateFriendshipStatusPort;
import com.astra.social.domain.AlreadyFriendsException;
import com.astra.social.domain.Friendship;
import com.astra.social.domain.FriendshipStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FriendshipServicesTest {

    private static final UUID ANA = UUID.randomUUID();
    private static final UUID BIA = UUID.randomUUID();
    private static final UUID CAIO = UUID.randomUUID();
    private static final UUID FRIENDSHIP_ID = UUID.randomUUID();
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.parse("2026-11-01T10:00:00Z");

    private final FindUserByHandlePort findUserByHandlePort = mock(FindUserByHandlePort.class);
    private final LoadFriendshipPort loadPort = mock(LoadFriendshipPort.class);
    private final CreateFriendshipPort createPort = mock(CreateFriendshipPort.class);
    private final UpdateFriendshipStatusPort updateStatusPort = mock(UpdateFriendshipStatusPort.class);
    private final DeleteFriendshipPort deletePort = mock(DeleteFriendshipPort.class);
    private final ListFriendshipsPort listPort = mock(ListFriendshipsPort.class);
    private final LoadUserSummariesPort summariesPort = mock(LoadUserSummariesPort.class);

    private final SendFriendRequestService sendService = new SendFriendRequestService(
            findUserByHandlePort, loadPort, createPort, updateStatusPort, summariesPort);
    private final AcceptFriendRequestService acceptService =
            new AcceptFriendRequestService(loadPort, updateStatusPort, summariesPort);
    private final RemoveFriendshipService removeService = new RemoveFriendshipService(loadPort, deletePort);
    private final FriendshipQueryService queryService =
            new FriendshipQueryService(listPort, loadPort, summariesPort);

    @BeforeEach
    void users() {
        when(summariesPort.loadByIds(anyCollection())).thenReturn(Map.of(
                ANA, new UserSummary(ANA, "Ana", "0001", "bio da ana", false),
                BIA, new UserSummary(BIA, "Bia", "0002", null, true)));
    }

    private Friendship stored(UUID requester, UUID addressee, FriendshipStatus status) {
        Friendship friendship = Friendship.withId(FRIENDSHIP_ID, requester, addressee, status, CREATED_AT);
        when(loadPort.load(FRIENDSHIP_ID)).thenReturn(Optional.of(friendship));
        when(loadPort.loadBetween(requester, addressee)).thenReturn(Optional.of(friendship));
        when(loadPort.loadBetween(addressee, requester)).thenReturn(Optional.of(friendship));
        return friendship;
    }

    @Test
    void novoConviteCriaPendenteComDadosDoOutro() {
        when(findUserByHandlePort.findIdByHandle("Bia", "0002")).thenReturn(Optional.of(BIA));
        when(createPort.create(any())).thenAnswer(invocation -> {
            Friendship f = invocation.getArgument(0);
            return Friendship.withId(FRIENDSHIP_ID, f.getRequesterId(), f.getAddresseeId(), f.getStatus(), CREATED_AT);
        });

        FriendshipResponse response = sendService.send(new SendFriendRequestCommand(ANA, "Bia#0002"));

        assertThat(response.friendUserId()).isEqualTo(BIA);
        assertThat(response.friendName()).isEqualTo("Bia");
        assertThat(response.friendAdmin()).isTrue();
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.incoming()).isFalse();
    }

    @Test
    void handleInexistenteNaoCriaNada() {
        when(findUserByHandlePort.findIdByHandle("Zé", "9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sendService.send(new SendFriendRequestCommand(ANA, "Zé#9999")))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(createPort, updateStatusPort);
    }

    @Test
    void conviteCruzadoAceitaEGravaOStatus() {
        when(findUserByHandlePort.findIdByHandle("Ana", "0001")).thenReturn(Optional.of(ANA));
        Friendship friendship = stored(ANA, BIA, FriendshipStatus.PENDING);

        FriendshipResponse response = sendService.send(new SendFriendRequestCommand(BIA, "Ana#0001"));

        assertThat(response.status()).isEqualTo("ACCEPTED");
        verify(updateStatusPort).updateStatus(friendship);
        verify(createPort, never()).create(any());
    }

    @Test
    void conviteParaQuemJaEAmigoNaoGravaNada() {
        when(findUserByHandlePort.findIdByHandle("Bia", "0002")).thenReturn(Optional.of(BIA));
        stored(ANA, BIA, FriendshipStatus.ACCEPTED);

        assertThatThrownBy(() -> sendService.send(new SendFriendRequestCommand(ANA, "Bia#0002")))
                .isInstanceOf(AlreadyFriendsException.class);
        verifyNoInteractions(createPort, updateStatusPort);
    }

    @Test
    void soOConvidadoAceita() {
        stored(ANA, BIA, FriendshipStatus.PENDING);

        assertThatThrownBy(() -> acceptService.accept(ANA, FRIENDSHIP_ID)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> acceptService.accept(CAIO, FRIENDSHIP_ID)).isInstanceOf(NotFoundException.class);
        verify(updateStatusPort, never()).updateStatus(any());

        FriendshipResponse response = acceptService.accept(BIA, FRIENDSHIP_ID);
        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.incoming()).isTrue();
        assertThat(response.friendUserId()).isEqualTo(ANA);
    }

    @Test
    void osDoisLadosRemovemETerceiroNao() {
        stored(ANA, BIA, FriendshipStatus.ACCEPTED);

        assertThatThrownBy(() -> removeService.remove(CAIO, FRIENDSHIP_ID)).isInstanceOf(NotFoundException.class);
        verify(deletePort, never()).delete(any());

        removeService.remove(BIA, FRIENDSHIP_ID);
        verify(deletePort).delete(FRIENDSHIP_ID);
    }

    @Test
    void idsDeAmigosEConferenciaDeAmizade() {
        when(listPort.listAccepted(ANA)).thenReturn(List.of(
                Friendship.withId(UUID.randomUUID(), ANA, BIA, FriendshipStatus.ACCEPTED, CREATED_AT),
                Friendship.withId(UUID.randomUUID(), CAIO, ANA, FriendshipStatus.ACCEPTED, CREATED_AT)));
        stored(ANA, BIA, FriendshipStatus.PENDING);

        assertThat(queryService.friendIdsOf(ANA)).containsExactly(BIA, CAIO);
        assertThat(queryService.areFriends(ANA, BIA)).isFalse();
        assertThat(queryService.areFriends(ANA, CAIO)).isFalse();
    }

    @Test
    void listaVaziaNaoBuscaUsuarios() {
        when(listPort.listPending(ANA)).thenReturn(List.of());

        assertThat(queryService.pendingRequests(ANA)).isEmpty();
        verify(summariesPort, never()).loadByIds(anyCollection());
    }

    @Test
    void exportacaoUsaNomeComTagEQuemEnviou() {
        when(listPort.listAll(ANA)).thenReturn(List.of(
                Friendship.withId(FRIENDSHIP_ID, BIA, ANA, FriendshipStatus.PENDING, CREATED_AT)));

        assertThat(queryService.export(ANA)).containsExactly(
                new FriendshipExport(BIA, "Bia#0002", "PENDING", false, CREATED_AT));
    }
}
