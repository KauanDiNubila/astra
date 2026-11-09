package com.astra.social.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.astra.TestcontainersConfiguration;
import com.astra.social.domain.Friendship;
import com.astra.social.domain.FriendshipStatus;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, FriendshipPersistenceAdapter.class, FriendshipMapper.class})
class FriendshipPersistenceAdapterTest {

    @Autowired
    private FriendshipPersistenceAdapter adapter;

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
    void criaECarregaPeloIdEEntreOsDoisEmQualquerOrdem() {
        UUID ana = insertUser();
        UUID bia = insertUser();

        Friendship created = adapter.create(Friendship.request(ana, bia));
        flushAndClear();

        assertThat(created.getId()).isNotNull();
        assertThat(created.getCreatedAt()).isNotNull();
        Friendship loaded = adapter.load(created.getId()).orElseThrow();
        assertThat(loaded.getStatus()).isEqualTo(FriendshipStatus.PENDING);
        assertThat(loaded.isRequester(ana)).isTrue();
        assertThat(adapter.loadBetween(ana, bia)).isPresent();
        assertThat(adapter.loadBetween(bia, ana)).isPresent();
        assertThat(adapter.loadBetween(ana, insertUser())).isEmpty();
    }

    @Test
    void gravaOStatusEListaPorSituacao() {
        UUID ana = insertUser();
        UUID bia = insertUser();
        UUID caio = insertUser();
        Friendship withBia = adapter.create(Friendship.request(ana, bia));
        adapter.create(Friendship.request(caio, ana));
        flushAndClear();

        Friendship loaded = adapter.load(withBia.getId()).orElseThrow();
        loaded.accept();
        adapter.updateStatus(loaded);
        flushAndClear();

        assertThat(adapter.listAccepted(ana)).extracting(f -> f.otherUserId(ana)).containsExactly(bia);
        assertThat(adapter.listAccepted(bia)).extracting(f -> f.otherUserId(bia)).containsExactly(ana);
        assertThat(adapter.listPending(ana)).extracting(f -> f.otherUserId(ana)).containsExactly(caio);
        assertThat(adapter.listAll(ana)).hasSize(2);
    }

    @Test
    void apaga() {
        UUID ana = insertUser();
        Friendship created = adapter.create(Friendship.request(ana, insertUser()));
        flushAndClear();

        adapter.delete(created.getId());
        flushAndClear();

        assertThat(adapter.load(created.getId())).isEmpty();
    }
}
