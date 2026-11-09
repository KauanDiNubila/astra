package com.astra.roadmap.adapter.out.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "roadmap")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
class RoadmapJpaEntity {

    @Id
    private UUID id;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 200)
    private String source;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    @Fetch(FetchMode.SUBSELECT)
    private List<StepJpaEntity> steps = new ArrayList<>();
}
