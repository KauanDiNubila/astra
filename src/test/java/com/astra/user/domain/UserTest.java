package com.astra.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.astra.shared.exception.ConflictException;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-11-09T12:00:00Z");

    private static User user(Role role) {
        return User.withId(UUID.randomUUID(), "Nome", "nome@astra.local", "hash", null, role, null, "0001", null,
                null, NOW);
    }

    @Test
    void cadastroComSenhaComecaComoUsuarioSemTermos() {
        User user = User.register("Ana", "ana@astra.local", "hash", "0042");

        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.hasPassword()).isTrue();
        assertThat(user.hasAcceptedCurrentTerms()).isFalse();
        user.acceptTerms(NOW);
        assertThat(user.hasAcceptedCurrentTerms()).isTrue();
        assertThat(user.getTermsAcceptedAt()).isEqualTo(NOW);
    }

    @Test
    void contaDoOAuthNasceSemSenhaEComTermosAceitos() {
        User user = User.fromOAuth("Ana", "ana@astra.local", "0042", NOW);

        assertThat(user.hasPassword()).isFalse();
        assertThat(user.hasAcceptedCurrentTerms()).isTrue();
        assertThatThrownBy(() -> user.changePasswordHash("novo")).isInstanceOf(ConflictException.class);
    }

    @Test
    void papeisTemHierarquia() {
        assertThat(Role.OWNER.isAbove(Role.ADMIN)).isTrue();
        assertThat(Role.ADMIN.isAbove(Role.ADMIN)).isFalse();
        assertThat(Role.USER.isStaff()).isFalse();
        assertThat(Role.ADMIN.isStaff()).isTrue();
        assertThat(Role.OWNER.isStaff()).isTrue();
        assertThat(Role.OWNER.withLowerRoles()).containsExactly(Role.OWNER, Role.ADMIN, Role.USER);
        assertThat(Role.USER.withLowerRoles()).containsExactly(Role.USER);
    }

    @Test
    void soQuemEstaAcimaBaneEDesbane() {
        User admin = user(Role.ADMIN);
        User otherAdmin = user(Role.ADMIN);
        User regular = user(Role.USER);

        admin.ban(regular, NOW);
        assertThat(regular.isBanned()).isTrue();
        admin.unban(regular);
        assertThat(regular.isBanned()).isFalse();

        assertThatThrownBy(() -> admin.ban(otherAdmin, NOW))
                .hasMessage("Você não tem permissão para banir esse usuário");
        assertThatThrownBy(() -> admin.ban(admin, NOW)).hasMessage("Não é possível banir a própria conta");
        assertThatThrownBy(() -> regular.unban(admin)).hasMessage("Você não tem permissão para desbanir esse usuário");
        assertThat(otherAdmin.isBanned()).isFalse();
    }

    @Test
    void excluirOutraContaSegueAHierarquia() {
        User owner = user(Role.OWNER);
        User admin = user(Role.ADMIN);

        owner.checkCanDelete(admin);
        assertThatThrownBy(() -> admin.checkCanDelete(owner)).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> admin.checkCanDelete(admin)).hasMessage("Não é possível excluir a própria conta");
    }

    @Test
    void soODonoPromoveUsuarioERebaixaAdmin() {
        User owner = user(Role.OWNER);
        User admin = user(Role.ADMIN);
        User regular = user(Role.USER);

        assertThatThrownBy(() -> admin.promote(regular)).hasMessage("Apenas o owner pode promover usuários a admin");
        owner.promote(regular);
        assertThat(regular.getRole()).isEqualTo(Role.ADMIN);
        assertThatThrownBy(() -> owner.promote(regular))
                .hasMessage("Só é possível promover usuários comuns a admin");

        assertThatThrownBy(() -> admin.demote(regular)).hasMessage("Apenas o owner pode remover admins");
        owner.demote(regular);
        assertThat(regular.getRole()).isEqualTo(Role.USER);
        assertThatThrownBy(() -> owner.demote(regular))
                .hasMessage("Só é possível remover privilégios de administradores");
    }

    @Test
    void donoNaoExcluiAPropriaContaEConfirmacaoPorEmailIgnoraCaixaEEspacos() {
        assertThatThrownBy(() -> user(Role.OWNER).checkCanDeleteOwnAccount()).isInstanceOf(ConflictException.class);
        user(Role.ADMIN).checkCanDeleteOwnAccount();

        User user = user(Role.USER);
        assertThat(user.isConfirmedByEmail("  NOME@astra.local ")).isTrue();
        assertThat(user.isConfirmedByEmail("outro@astra.local")).isFalse();
        assertThat(user.isConfirmedByEmail(null)).isFalse();
    }

    @Test
    void avatarPrecisaSerImagemDeAte2MB() {
        assertThat(Avatar.of("image/png", new byte[Avatar.MAX_BYTES]).contentType()).isEqualTo("image/png");
        assertThatThrownBy(() -> Avatar.of("text/plain", new byte[1])).hasMessage("Arquivo precisa ser uma imagem");
        assertThatThrownBy(() -> Avatar.of(null, new byte[1])).hasMessage("Arquivo precisa ser uma imagem");
        assertThatThrownBy(() -> Avatar.of("image/png", new byte[Avatar.MAX_BYTES + 1]))
                .hasMessage("Imagem precisa ter até 2MB");
    }
}
