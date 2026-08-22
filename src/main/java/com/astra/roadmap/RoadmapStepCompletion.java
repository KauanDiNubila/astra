package com.astra.roadmap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "roadmap_step_completion")
@IdClass(RoadmapStepCompletionId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadmapStepCompletion {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Id
    @Column(name = "step_id", nullable = false)
    private UUID stepId;

    @CreationTimestamp
    @Column(name = "completed_at", nullable = false)
    private OffsetDateTime completedAt;

    public RoadmapStepCompletion(UUID userId, UUID stepId) {
        this.userId = userId;
        this.stepId = stepId;
    }
}
