package com.astra.user.adapter.out.persistence;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByNameAndTag(String name, String tag);

    @Query("select u.id from UserJpaEntity u where u.name = :name and u.tag = :tag")
    Optional<UUID> findIdByNameAndTag(@Param("name") String name, @Param("tag") String tag);

    @Query("select u.role as role, u.bannedAt as bannedAt from UserJpaEntity u where u.id = :id")
    Optional<AuthInfoView> findAuthInfoById(@Param("id") UUID id);

    @Query("select u.id as id, u.name as name, u.bio as bio, u.role as role, u.tag as tag "
            + "from UserJpaEntity u where u.id in :ids")
    List<SummaryView> findSummariesByIdIn(@Param("ids") Collection<UUID> ids);

    @Query("select u.id as id, u.name as name, u.email as email, u.role as role, "
            + "u.bannedAt as bannedAt, u.createdAt as createdAt from UserJpaEntity u")
    List<AdminView> findAllForAdmin();

    interface AuthInfoView {
        String getRole();

        OffsetDateTime getBannedAt();
    }

    interface SummaryView {
        UUID getId();

        String getName();

        String getBio();

        String getRole();

        String getTag();
    }

    interface AdminView {
        UUID getId();

        String getName();

        String getEmail();

        String getRole();

        OffsetDateTime getBannedAt();

        OffsetDateTime getCreatedAt();
    }
}
