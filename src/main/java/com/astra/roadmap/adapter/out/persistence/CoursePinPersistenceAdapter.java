package com.astra.roadmap.adapter.out.persistence;

import com.astra.roadmap.application.port.out.DeleteCoursePinPort;
import com.astra.roadmap.application.port.out.LoadCoursePinPort;
import com.astra.roadmap.application.port.out.SaveCoursePinPort;
import com.astra.roadmap.domain.CoursePin;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CoursePinPersistenceAdapter implements LoadCoursePinPort, SaveCoursePinPort, DeleteCoursePinPort {

    private final SpringDataCoursePinRepository repository;
    private final EntityManager entityManager;

    CoursePinPersistenceAdapter(SpringDataCoursePinRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<CoursePin> load(UUID pinId) {
        return repository.findById(pinId).map(CoursePinPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<CoursePin> loadOn(UUID stepId, UUID courseId) {
        return repository.findByStepIdAndCourseId(stepId, courseId).map(CoursePinPersistenceAdapter::toDomain);
    }

    @Override
    public List<CoursePin> listOn(Collection<UUID> stepIds, Collection<UUID> courseIds) {
        return repository.findByStepIdInAndCourseIdIn(stepIds, courseIds).stream()
                .map(CoursePinPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<CoursePin> listForCourses(Collection<UUID> courseIds) {
        return repository.findByCourseIdIn(courseIds).stream().map(CoursePinPersistenceAdapter::toDomain).toList();
    }

    @Override
    public void create(CoursePin pin) {
        entityManager.persist(new CoursePinJpaEntity(pin.getId(), pin.getCourseId(), pin.getStepId(),
                pin.getStatus(), pin.getRating()));
    }

    @Override
    public void update(CoursePin pin) {
        CoursePinJpaEntity entity = repository.findById(pin.getId()).orElseThrow();
        entity.setStatus(pin.getStatus());
        entity.setRating(pin.getRating());
    }

    @Override
    public void delete(UUID pinId) {
        repository.deleteById(pinId);
    }

    private static CoursePin toDomain(CoursePinJpaEntity entity) {
        return CoursePin.withId(entity.getId(), entity.getCourseId(), entity.getStepId(), entity.getStatus(),
                entity.getRating());
    }
}
