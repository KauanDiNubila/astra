package com.astra.roadmap.adapter.out.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "roadmap_step")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class StepJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private RoadmapJpaEntity roadmap;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false)
    private int position;

    @Column(name = "parent_step_id")
    private UUID parentStepId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    @Fetch(FetchMode.SUBSELECT)
    private List<ResourceJpaEntity> resources = new ArrayList<>();

    StepJpaEntity(UUID id, RoadmapJpaEntity roadmap, String title, int position, UUID parentStepId,
                  String description) {
        this.id = id;
        this.roadmap = roadmap;
        this.title = title;
        this.position = position;
        this.parentStepId = parentStepId;
        this.description = description;
    }
}
