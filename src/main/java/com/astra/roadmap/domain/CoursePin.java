package com.astra.roadmap.domain;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CoursePin {

    private final UUID id;
    private final UUID courseId;
    private final UUID stepId;
    private String status;
    private Integer rating;

    private CoursePin(UUID id, UUID courseId, UUID stepId, String status, Integer rating) {
        this.id = Objects.requireNonNull(id);
        this.courseId = Objects.requireNonNull(courseId);
        this.stepId = Objects.requireNonNull(stepId);
        this.status = status;
        this.rating = rating;
    }

    public static CoursePin pin(UUID courseId, UUID stepId, String status, Integer rating) {
        return new CoursePin(UUID.randomUUID(), courseId, stepId, status, rating);
    }

    public static CoursePin withId(UUID id, UUID courseId, UUID stepId, String status, Integer rating) {
        return new CoursePin(id, courseId, stepId, status, rating);
    }

    public boolean isOn(UUID stepId) {
        return this.stepId.equals(stepId);
    }

    public void change(String status, Integer rating) {
        this.status = status;
        this.rating = rating;
    }
}
