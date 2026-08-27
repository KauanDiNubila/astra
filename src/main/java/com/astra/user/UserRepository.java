package com.astra.user;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    // As projeções abaixo existem porque User.avatar é um byte[] eager (até
    // 2MB) sem @Basic(fetch = LAZY) — carregar a entidade inteira em qualquer
    // um desses casos traria o avatar (e o hash da senha) à toa.

    // Roda no filtro de autenticação, ou seja, uma vez por requisição.
    @Query("select u.role as role, u.bannedAt as bannedAt from User u where u.id = :id")
    Optional<AuthInfoView> findAuthInfoById(@Param("id") UUID id);

    // Usado por /auth/refresh, /me e o construtor de UserResponse em geral.
    @Query("select u.id as id, u.name as name, u.email as email, u.bio as bio, u.role as role from User u where u.id = :id")
    Optional<UserSummaryView> findSummaryById(@Param("id") UUID id);

    // Usado em lote por amigos/chat/ranking pra evitar 1 findById por pessoa.
    // Inclui role pra dar pra mostrar o selo de admin nessas listas.
    @Query("select u.id as id, u.name as name, u.bio as bio, u.role as role from User u where u.id in :ids")
    List<NameBioView> findNameBioByIdIn(@Param("ids") Collection<UUID> ids);

    // Lista de administração — mesmo motivo, sem o avatar de cada usuário.
    @Query("select u.id as id, u.name as name, u.email as email, u.role as role, "
            + "u.bannedAt as bannedAt, u.createdAt as createdAt from User u")
    List<AdminSummaryView> findAllAdminSummaries();

    interface AuthInfoView {
        String getRole();

        OffsetDateTime getBannedAt();
    }

    interface UserSummaryView {
        UUID getId();

        String getName();

        String getEmail();

        String getBio();

        String getRole();
    }

    interface NameBioView {
        UUID getId();

        String getName();

        String getBio();

        String getRole();
    }

    interface AdminSummaryView {
        UUID getId();

        String getName();

        String getEmail();

        String getRole();

        OffsetDateTime getBannedAt();

        OffsetDateTime getCreatedAt();
    }
}
