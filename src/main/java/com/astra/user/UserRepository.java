package com.astra.user;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    // Roda no filtro de autenticação, ou seja, uma vez por requisição: carregar
    // a entidade inteira aqui traria o avatar (byte[] eager, até 2MB) e o hash
    // da senha só pra ler duas colunas.
    @Query("select u.role as role, u.bannedAt as bannedAt from User u where u.id = :id")
    Optional<AuthInfoView> findAuthInfoById(@Param("id") UUID id);

    interface AuthInfoView {
        String getRole();

        OffsetDateTime getBannedAt();
    }
}
