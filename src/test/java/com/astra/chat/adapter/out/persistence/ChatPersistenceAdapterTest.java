package com.astra.chat.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.astra.TestcontainersConfiguration;
import com.astra.chat.domain.ChatGroup;
import com.astra.chat.domain.ChatImage;
import com.astra.chat.domain.Destination;
import com.astra.chat.domain.Message;
import com.astra.shared.crypto.EncryptionService;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, EncryptionService.class, MessagePersistenceAdapter.class,
        GroupPersistenceAdapter.class, GroupPhotoPersistenceAdapter.class})
class ChatPersistenceAdapterTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-11-10T12:00:00Z");

    @Autowired
    private MessagePersistenceAdapter messages;

    @Autowired
    private GroupPersistenceAdapter groups;

    @Autowired
    private GroupPhotoPersistenceAdapter photos;

    @Autowired
    private EntityManager entityManager;

    private UUID insertUser() {
        UUID id = UUID.randomUUID();
        entityManager.createNativeQuery(
                        "insert into users (id, name, email, password_hash, tag) values (?, ?, ?, 'x', '0001')")
                .setParameter(1, id)
                .setParameter(2, "user-" + id)
                .setParameter(3, id + "@astra.local")
                .executeUpdate();
        return id;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void textoEImagemFicamCifradosNoBancoEVoltamAbertos() {
        UUID ana = insertUser();
        UUID beto = insertUser();

        Message created = messages.create(Message.photo(ana, Destination.direct(beto), "segredo"),
                new ChatImage("image/png", new byte[] {1, 2, 3}));
        flushAndClear();

        Object storedText = entityManager.createNativeQuery("select content from message where id = ?")
                .setParameter(1, created.getId()).getSingleResult();
        byte[] storedBytes = (byte[]) entityManager.createNativeQuery(
                        "select data from message_attachment where message_id = ?")
                .setParameter(1, created.getId()).getSingleResult();
        assertThat(storedText.toString()).isNotEqualTo("segredo").doesNotContain("segredo");
        assertThat(storedBytes).isNotEqualTo(new byte[] {1, 2, 3});

        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(messages.load(created.getId()).orElseThrow().getContent()).isEqualTo("segredo");
        assertThat(messages.loadFor(created.getId()).orElseThrow().bytes()).containsExactly(1, 2, 3);
        assertThat(messages.attachmentIdsFor(List.of(created.getId()))).containsKey(created.getId());
        assertThat(messages.lastWith(beto, List.of(ana)).get(ana).content()).isEqualTo("segredo");
    }

    @Test
    void naoLidasEMarcarComoLida() {
        UUID ana = insertUser();
        UUID beto = insertUser();
        messages.create(Message.text(ana, Destination.direct(beto), "um"), null);
        messages.create(Message.text(ana, Destination.direct(beto), "dois"), null);
        flushAndClear();

        assertThat(messages.directTo(beto)).isEqualTo(2);
        assertThat(messages.directBySender(beto, List.of(ana))).containsEntry(ana, 2L);

        messages.markRead(ana, beto, NOW);
        flushAndClear();
        assertThat(messages.directTo(beto)).isZero();
        assertThat(messages.directConversation(ana, beto, 10)).allMatch(Message::isRead);
    }

    @Test
    void grupoComMembrosLeituraEFoto() {
        UUID ana = insertUser();
        UUID beto = insertUser();
        ChatGroup created = groups.create(ChatGroup.create("Estudo", ana, List.of(beto), NOW));
        flushAndClear();

        ChatGroup loaded = groups.load(created.getId()).orElseThrow();
        assertThat(loaded.memberIds()).containsExactlyInAnyOrder(ana, beto);
        assertThat(groups.listForMember(beto)).extracting(ChatGroup::getId).containsExactly(created.getId());
        assertThat(groups.nameOf(created.getId())).contains("Estudo");
        assertThat(groups.groupIdsOf(ana)).containsExactly(created.getId());
        assertThat(groups.markRead(created.getId(), beto, NOW.plusMinutes(1))).isTrue();
        assertThat(groups.markRead(created.getId(), insertUser(), NOW)).isFalse();

        assertThat(photos.load(created.getId())).isEmpty();
        photos.update(created.getId(), new ChatImage("image/png", new byte[] {9}));
        flushAndClear();
        assertThat(photos.load(created.getId()).orElseThrow().bytes()).containsExactly(9);
        assertThat(groups.load(created.getId())).isPresent();
    }
}
