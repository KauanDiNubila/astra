package com.astra.learning;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 80)
    private String platform;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CourseStatus status;

    public Course(UUID userId, String title, String platform, CourseStatus status) {
        this.userId = userId;
        this.title = title;
        this.platform = platform;
        this.status = status;
    }
}
