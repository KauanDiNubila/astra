package com.astra.social.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FriendshipTest {

    private static final UUID ANA = UUID.randomUUID();
    private static final UUID BIA = UUID.randomUUID();
    private static final UUID CAIO = UUID.randomUUID();

    @Test
    void conviteNascePendente() {
        Friendship friendship = Friendship.request(ANA, BIA);

        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.PENDING);
        assertThat(friendship.isAccepted()).isFalse();
        assertThat(friendship.getId()).isNull();
    }

    @Test
    void naoConvidaASiMesmo() {
        assertThatThrownBy(() -> Friendship.request(ANA, ANA)).isInstanceOf(CannotBefriendYourselfException.class);
    }

    @Test
    void sabeQuemEstaEnvolvidoEQuemEOOutro() {
        Friendship friendship = Friendship.request(ANA, BIA);

        assertThat(friendship.isRequester(ANA)).isTrue();
        assertThat(friendship.isAddressee(BIA)).isTrue();
        assertThat(friendship.involves(ANA)).isTrue();
        assertThat(friendship.involves(CAIO)).isFalse();
        assertThat(friendship.otherUserId(ANA)).isEqualTo(BIA);
        assertThat(friendship.otherUserId(BIA)).isEqualTo(ANA);
    }

    @Test
    void conviteDeVoltaAceita() {
        Friendship friendship = Friendship.request(ANA, BIA);

        friendship.receiveRequestFrom(BIA);

        assertThat(friendship.isAccepted()).isTrue();
    }

    @Test
    void mesmoRemetenteDeNovoERecusado() {
        Friendship friendship = Friendship.request(ANA, BIA);

        assertThatThrownBy(() -> friendship.receiveRequestFrom(ANA))
                .isInstanceOf(FriendRequestAlreadySentException.class);
        assertThat(friendship.isAccepted()).isFalse();
    }

    @Test
    void jaAmigosRecusaNovoConviteDosDoisLados() {
        Friendship friendship = Friendship.withId(UUID.randomUUID(), ANA, BIA, FriendshipStatus.ACCEPTED,
                OffsetDateTime.now());

        assertThatThrownBy(() -> friendship.receiveRequestFrom(ANA)).isInstanceOf(AlreadyFriendsException.class);
        assertThatThrownBy(() -> friendship.receiveRequestFrom(BIA)).isInstanceOf(AlreadyFriendsException.class);
    }

    @Test
    void aceitar() {
        Friendship friendship = Friendship.request(ANA, BIA);

        friendship.accept();

        assertThat(friendship.isAccepted()).isTrue();
    }

    @Test
    void amizadeCarregadaPrecisaDeId() {
        assertThatThrownBy(() -> Friendship.withId(null, ANA, BIA, FriendshipStatus.PENDING, null))
                .isInstanceOf(NullPointerException.class);
    }
}
