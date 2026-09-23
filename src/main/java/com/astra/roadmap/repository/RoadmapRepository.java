package com.astra.roadmap.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.astra.roadmap.entity.Roadmap;

public interface RoadmapRepository extends JpaRepository<Roadmap, UUID> {

    List<Roadmap> findByOwnerIdIsNullOrOwnerId(UUID ownerId);

    Optional<Roadmap> findByIdAndOwnerId(UUID id, UUID ownerId);
}
