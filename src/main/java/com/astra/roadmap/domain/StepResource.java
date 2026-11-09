package com.astra.roadmap.domain;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class StepResource {

    private final UUID id;
    private final String label;
    private final String url;
    private final int position;

    private StepResource(UUID id, String label, String url, int position) {
        this.id = Objects.requireNonNull(id);
        this.label = Objects.requireNonNull(label);
        this.url = Objects.requireNonNull(url);
        this.position = position;
    }

    static StepResource create(String label, String url, int position) {
        return new StepResource(UUID.randomUUID(), label, url, position);
    }

    public static StepResource withId(UUID id, String label, String url, int position) {
        return new StepResource(id, label, url, position);
    }
}
