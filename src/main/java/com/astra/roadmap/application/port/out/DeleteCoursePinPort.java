package com.astra.roadmap.application.port.out;

import java.util.UUID;

public interface DeleteCoursePinPort {

    void delete(UUID pinId);
}
