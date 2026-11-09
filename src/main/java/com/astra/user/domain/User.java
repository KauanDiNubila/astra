package com.astra.user.domain;

import com.astra.shared.exception.ConflictException;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class User {

    private final UUID id;
    private String name;
    private final String email;
    private String passwordHash;
    private String bio;
    private Role role;
    private OffsetDateTime bannedAt;
    private String tag;
    private OffsetDateTime termsAcceptedAt;
    private String termsVersion;
    private final OffsetDateTime createdAt;

    private User(UUID id, String name, String email, String passwordHash, String bio, Role role,
                 OffsetDateTime bannedAt, String tag, OffsetDateTime termsAcceptedAt, String termsVersion,
                 OffsetDateTime createdAt) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = passwordHash;
        this.bio = bio;
        this.role = Objects.requireNonNull(role);
        this.bannedAt = bannedAt;
        this.tag = Objects.requireNonNull(tag);
        this.termsAcceptedAt = termsAcceptedAt;
        this.termsVersion = termsVersion;
        this.createdAt = createdAt;
    }

    public static User register(String name, String email, String passwordHash, String tag) {
        return new User(null, name, email, Objects.requireNonNull(passwordHash), null, Role.USER, null, tag, null,
                null, null);
    }

    public static User fromOAuth(String name, String email, String tag, OffsetDateTime now) {
        User user = new User(null, name, email, null, null, Role.USER, null, tag, null, null, null);
        user.acceptTerms(now);
        return user;
    }

    public static User withId(UUID id, String name, String email, String passwordHash, String bio, Role role,
                              OffsetDateTime bannedAt, String tag, OffsetDateTime termsAcceptedAt,
                              String termsVersion, OffsetDateTime createdAt) {
        return new User(Objects.requireNonNull(id), name, email, passwordHash, bio, role, bannedAt, tag,
                termsAcceptedAt, termsVersion, createdAt);
    }

    public boolean hasPassword() {
        return passwordHash != null;
    }

    public boolean isBanned() {
        return bannedAt != null;
    }

    public boolean hasAcceptedCurrentTerms() {
        return TermsPolicy.CURRENT_VERSION.equals(termsVersion);
    }

    public void acceptTerms(OffsetDateTime now) {
        this.termsAcceptedAt = now;
        this.termsVersion = TermsPolicy.CURRENT_VERSION;
    }

    public void changeProfile(String name, String tag, String bio) {
        this.name = Objects.requireNonNull(name);
        this.tag = Objects.requireNonNull(tag);
        this.bio = bio;
    }

    public void requirePassword() {
        if (!hasPassword()) {
            throw new ConflictException("Essa conta não tem senha definida (entrou via Google/GitHub)");
        }
    }

    public void changePasswordHash(String passwordHash) {
        requirePassword();
        this.passwordHash = Objects.requireNonNull(passwordHash);
    }

    public void checkCanDeleteOwnAccount() {
        if (role == Role.OWNER) {
            throw new ConflictException("A conta owner não pode ser excluída por aqui");
        }
    }

    public boolean isConfirmedByEmail(String confirmation) {
        return confirmation != null && confirmation.trim().equalsIgnoreCase(email);
    }

    public void ban(User target, OffsetDateTime now) {
        if (target.id.equals(id)) {
            throw new ConflictException("Não é possível banir a própria conta");
        }
        requireAbove(target, "banir");
        target.bannedAt = now;
    }

    public void unban(User target) {
        requireAbove(target, "desbanir");
        target.bannedAt = null;
    }

    public void checkCanDelete(User target) {
        if (target.id.equals(id)) {
            throw new ConflictException("Não é possível excluir a própria conta");
        }
        requireAbove(target, "excluir");
    }

    public void promote(User target) {
        if (role != Role.OWNER) {
            throw new ConflictException("Apenas o owner pode promover usuários a admin");
        }
        if (target.role != Role.USER) {
            throw new ConflictException("Só é possível promover usuários comuns a admin");
        }
        target.role = Role.ADMIN;
    }

    public void demote(User target) {
        if (role != Role.OWNER) {
            throw new ConflictException("Apenas o owner pode remover admins");
        }
        if (target.role != Role.ADMIN) {
            throw new ConflictException("Só é possível remover privilégios de administradores");
        }
        target.role = Role.USER;
    }

    private void requireAbove(User target, String action) {
        if (!role.isAbove(target.role)) {
            throw new ConflictException("Você não tem permissão para " + action + " esse usuário");
        }
    }
}
