package com.astra.learning;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByModuleIdInOrderByPosition(Collection<UUID> moduleIds);

    Optional<Lesson> findByIdAndModuleId(UUID id, UUID moduleId);

    long countByModuleIdIn(Collection<UUID> moduleIds);

    long countByModuleIdInAndCompletedTrue(Collection<UUID> moduleIds);
}
