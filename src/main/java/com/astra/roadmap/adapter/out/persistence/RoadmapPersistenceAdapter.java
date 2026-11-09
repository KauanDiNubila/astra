package com.astra.roadmap.adapter.out.persistence;

import com.astra.roadmap.application.port.out.CreateRoadmapPort;
import com.astra.roadmap.application.port.out.ListRoadmapsPort;
import com.astra.roadmap.application.port.out.LoadRoadmapPort;
import com.astra.roadmap.application.port.out.LoadStepPort;
import com.astra.roadmap.application.port.out.UpdateRoadmapPort;
import com.astra.roadmap.domain.LocatedStep;
import com.astra.roadmap.domain.Roadmap;
import com.astra.roadmap.domain.RoadmapOutline;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class RoadmapPersistenceAdapter implements CreateRoadmapPort, LoadRoadmapPort, UpdateRoadmapPort, ListRoadmapsPort,
        LoadStepPort {

    private final SpringDataRoadmapRepository roadmapRepository;
    private final SpringDataStepRepository stepRepository;
    private final RoadmapMapper mapper;
    private final EntityManager entityManager;

    RoadmapPersistenceAdapter(SpringDataRoadmapRepository roadmapRepository, SpringDataStepRepository stepRepository,
                              RoadmapMapper mapper, EntityManager entityManager) {
        this.roadmapRepository = roadmapRepository;
        this.stepRepository = stepRepository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public void create(Roadmap roadmap) {
        entityManager.persist(mapper.toNewJpaEntity(roadmap));
    }

    @Override
    public Optional<Roadmap> load(UUID roadmapId) {
        return roadmapRepository.findById(roadmapId).map(mapper::toDomain);
    }

    @Override
    public Optional<Roadmap> loadOwnedByStep(UUID stepId, UUID userId) {
        return roadmapRepository.findOwnedByStep(stepId, userId).map(mapper::toDomain);
    }

    @Override
    public void update(Roadmap roadmap) {
        mapper.copySteps(roadmap, roadmapRepository.findById(roadmap.getId()).orElseThrow());
    }

    @Override
    public List<RoadmapOutline> listOutlinesFor(UUID userId) {
        return roadmapRepository.findByOwnerIdIsNullOrOwnerId(userId).stream().map(mapper::toOutline).toList();
    }

    @Override
    public Optional<RoadmapOutline> loadOutline(UUID roadmapId) {
        return roadmapRepository.findById(roadmapId).map(mapper::toOutline);
    }

    @Override
    public List<Roadmap> listOwnedBy(UUID userId) {
        return roadmapRepository.findByOwnerId(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<LocatedStep> loadStep(UUID stepId) {
        return stepRepository.findWithRoadmap(stepId).map(mapper::toLocated);
    }
}
