package com.astra.user.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataUserAvatarRepository extends JpaRepository<UserAvatarJpaEntity, UUID> {
}
