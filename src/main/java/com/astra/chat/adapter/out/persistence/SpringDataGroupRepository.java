package com.astra.chat.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataGroupRepository extends JpaRepository<GroupJpaEntity, UUID> {

    @Query("select g.name from GroupJpaEntity g where g.id = :id")
    Optional<String> findNameById(@Param("id") UUID id);
}
