package com.astra.user.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.astra.TestcontainersConfiguration;
import com.astra.user.application.port.in.UserSummary;
import com.astra.user.domain.Avatar;
import com.astra.user.domain.OAuthConnection;
import com.astra.user.domain.Role;
import com.astra.user.domain.User;
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
@Import({TestcontainersConfiguration.class, UserPersistenceAdapter.class, UserMapper.class,
        AvatarPersistenceAdapter.class, OAuthConnectionPersistenceAdapter.class})
class UserPersistenceAdapterTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-11-09T12:00:00Z");

    @Autowired
    private UserPersistenceAdapter users;

    @Autowired
    private AvatarPersistenceAdapter avatars;

    @Autowired
    private OAuthConnectionPersistenceAdapter connections;

    @Autowired
    private EntityManager entityManager;

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private User created(String name) {
        User user = users.create(User.register(name, name + "-" + UUID.randomUUID() + "@astra.local", "hash",
                "0042"));
        flushAndClear();
        return user;
    }

    @Test
    void criaComIdEDataECarregaPorIdEEmail() {
        User user = created("Ana");

        assertThat(user.getId()).isNotNull();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(users.load(user.getId()).orElseThrow().getName()).isEqualTo("Ana");
        assertThat(users.loadByEmail(user.getEmail()).orElseThrow().getId()).isEqualTo(user.getId());
        assertThat(users.isTaken("Ana", "0042")).isTrue();
        assertThat(users.findIdByHandle("Ana", "0042")).isPresent();
    }

    @Test
    void gravaPerfilPapelBanimentoETermos() {
        User user = created("Bia");
        User loaded = users.load(user.getId()).orElseThrow();
        loaded.changeProfile("Bia Nova", "0007", "bio");
        loaded.acceptTerms(NOW);
        User owner = User.withId(UUID.randomUUID(), "Dono", "d@astra.local", "h", null, Role.OWNER, null, "0001",
                null, null, NOW);
        owner.promote(loaded);
        owner.ban(loaded, NOW);
        users.update(loaded);
        flushAndClear();

        User reloaded = users.load(user.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Bia Nova");
        assertThat(reloaded.getTag()).isEqualTo("0007");
        assertThat(reloaded.getRole()).isEqualTo(Role.ADMIN);
        assertThat(reloaded.isBanned()).isTrue();
        assertThat(reloaded.hasAcceptedCurrentTerms()).isTrue();
        assertThat(users.loadAuthInfo(user.getId()).orElseThrow().roles()).containsExactly("ADMIN", "USER");
        assertThat(users.loadAuthInfo(user.getId()).orElseThrow().banned()).isTrue();
    }

    @Test
    void avatarFicaForaDoUsuarioESoDescePeloSeuAdaptador() {
        User user = created("Foto");
        assertThat(avatars.load(user.getId())).isEmpty();

        avatars.save(user.getId(), Avatar.of("image/png", new byte[] {1, 2, 3}));
        flushAndClear();

        assertThat(avatars.load(user.getId()).orElseThrow().bytes()).containsExactly(1, 2, 3);
        assertThat(users.load(user.getId())).isPresent();
    }

    @Test
    void resumosTrazemSeloDeStaffEListaDeAdminTodos() {
        User user = created("Resumo");

        assertThat(users.loadSummaries(List.of(user.getId())).get(user.getId()))
                .isEqualTo(new UserSummary(user.getId(), "Resumo", "0042", null, false));
        assertThat(users.listForAdmin()).anyMatch(a -> a.id().equals(user.getId()) && !a.banned());
        assertThat(users.loadResponse(user.getId()).orElseThrow().hasPassword()).isTrue();
    }

    @Test
    void conexaoOAuthAchaODonoEApagarUsuarioFunciona() {
        User user = created("OAuth");
        connections.create(OAuthConnection.link(user.getId(), "google", "sub-1"));
        flushAndClear();

        assertThat(connections.findUserId("google", "sub-1")).contains(user.getId());
        assertThat(connections.findUserId("github", "sub-1")).isEmpty();
        assertThat(connections.listByUser(user.getId())).singleElement()
                .satisfies(c -> assertThat(c.createdAt()).isNotNull());

        users.delete(user.getId());
        flushAndClear();
        assertThat(users.load(user.getId())).isEmpty();
    }
}
