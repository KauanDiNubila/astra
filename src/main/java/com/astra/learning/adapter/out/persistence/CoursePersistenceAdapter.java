package com.astra.learning.adapter.out.persistence;

import com.astra.learning.application.port.out.CheckCourseOwnershipPort;
import com.astra.learning.application.port.out.CreateCoursePort;
import com.astra.learning.application.port.out.ListCoursesPort;
import com.astra.learning.application.port.out.LoadCoursePort;
import com.astra.learning.application.port.out.UpdateCoursePort;
import com.astra.learning.domain.Course;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CoursePersistenceAdapter implements CreateCoursePort, LoadCoursePort, ListCoursesPort, UpdateCoursePort,
        CheckCourseOwnershipPort {

    private final SpringDataCourseRepository repository;
    private final CourseMapper mapper;
    private final EntityManager entityManager;

    CoursePersistenceAdapter(SpringDataCourseRepository repository, CourseMapper mapper,
                             EntityManager entityManager) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public void create(Course course) {
        entityManager.persist(mapper.toNewJpaEntity(course));
    }

    @Override
    public Optional<Course> loadOwned(UUID courseId, UUID userId) {
        return repository.findByIdAndUserId(courseId, userId).map(mapper::toDomain);
    }

    @Override
    public List<Course> listByUser(UUID userId) {
        return repository.findByUserId(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void update(Course course) {
        CourseJpaEntity entity = repository.findById(course.getId()).orElseThrow();
        mapper.copyModules(course, entity);
    }

    @Override
    public boolean isOwner(UUID courseId, UUID userId) {
        return repository.existsByIdAndUserId(courseId, userId);
    }

    @Override
    public List<UUID> courseIdsOf(UUID userId) {
        return repository.findIdsByUserId(userId);
    }
}
