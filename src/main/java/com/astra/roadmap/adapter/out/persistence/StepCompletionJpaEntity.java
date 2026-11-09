package com.astra.roadmap.adapter.out.persistence;

import com.astra.roadmap.domain.StepStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "roadmap_step_completion")
@IdClass(StepCompletionJpaEntity.Key.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class StepCompletionJpaEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Id
    @Column(name = "step_id", nullable = false)
    private UUID stepId;

    @CreationTimestamp
    @Column(name = "completed_at", nullable = false)
    private OffsetDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StepStatus status;

    StepCompletionJpaEntity(UUID userId, UUID stepId, StepStatus status) {
        this.userId = userId;
        this.stepId = stepId;
        this.status = status;
    }

    record Key(UUID userId, UUID stepId) implements Serializable {

        Key() {
            this(null, null);
        }
    }
}
