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
@Table(name = "course_step_link")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseStepLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "step_id", nullable = false)
    private RoadmapStep step;

    @Column(length = 30)
    private String status;

    private Integer rating;

    public CourseStepLink(UUID courseId, RoadmapStep step, String status, Integer rating) {
        this.courseId = courseId;
        this.step = step;
        this.status = status;
        this.rating = rating;
    }
}
