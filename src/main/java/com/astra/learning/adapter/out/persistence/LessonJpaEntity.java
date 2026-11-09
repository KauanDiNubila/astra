package com.astra.learning.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "lesson")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class LessonJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private ModuleJpaEntity module;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private boolean completed;

    LessonJpaEntity(UUID id, ModuleJpaEntity module) {
        this.id = id;
        this.module = module;
    }
}
