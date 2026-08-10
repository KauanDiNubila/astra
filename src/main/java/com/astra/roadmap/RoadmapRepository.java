package com.astra.roadmap;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapRepository extends JpaRepository<Roadmap, UUID> {

    List<Roadmap> findByOwnerIdIsNullOrOwnerId(UUID ownerId);

    Optional<Roadmap> findByIdAndOwnerId(UUID id, UUID ownerId);
}
