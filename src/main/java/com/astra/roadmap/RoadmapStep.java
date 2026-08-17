package com.astra.roadmap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roadmap_step")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadmapStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false)
    private int position;

    @Column(name = "parent_step_id")
    private UUID parentStepId;

    @Column(nullable = false)
    private boolean completed;

    public RoadmapStep(Roadmap roadmap, String title, int position) {
        this.roadmap = roadmap;
        this.title = title;
        this.position = position;
        this.completed = false;
    }

    public RoadmapStep(Roadmap roadmap, String title, int position, UUID parentStepId) {
        this.roadmap = roadmap;
        this.title = title;
        this.position = position;
        this.parentStepId = parentStepId;
        this.completed = false;
    }
}
