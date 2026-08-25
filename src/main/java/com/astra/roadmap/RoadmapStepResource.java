package com.astra.roadmap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roadmap_step_resource")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadmapStepResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "step_id", nullable = false)
    private UUID stepId;

    @Column(nullable = false, length = 160)
    private String label;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private int position;

    public RoadmapStepResource(UUID stepId, String label, String url, int position) {
        this.stepId = stepId;
        this.label = label;
        this.url = url;
        this.position = position;
    }
}
