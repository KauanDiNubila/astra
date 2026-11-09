package com.astra.learning.domain;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Lesson {

    private final UUID id;
    private final String title;
    private int position;
    private boolean completed;

    private Lesson(UUID id, String title, int position, boolean completed) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.position = position;
        this.completed = completed;
    }

    static Lesson create(String title, int position) {
        return new Lesson(UUID.randomUUID(), title, position, false);
    }

    public static Lesson withId(UUID id, String title, int position, boolean completed) {
        return new Lesson(id, title, position, completed);
    }

    void markCompleted(boolean completed) {
        this.completed = completed;
    }

    void moveTo(int position) {
        this.position = position;
    }
}
