package com.astra.chat.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataGroupPhotoRepository extends JpaRepository<GroupPhotoJpaEntity, UUID> {
}
