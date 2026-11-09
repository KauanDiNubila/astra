package com.astra.roadmap.application.service;

import com.astra.roadmap.application.port.in.UnpinCourseUseCase;
import com.astra.roadmap.application.port.out.DeleteCoursePinPort;
import com.astra.roadmap.application.port.out.LoadCoursePinPort;
import com.astra.roadmap.application.port.out.LoadStepPort;
import com.astra.roadmap.application.port.out.UserCoursesPort;
import com.astra.roadmap.domain.CoursePin;
import com.astra.shared.exception.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UnpinCourseService implements UnpinCourseUseCase {

    private final LoadStepPort loadStepPort;
    private final UserCoursesPort userCoursesPort;
    private final LoadCoursePinPort loadCoursePinPort;
    private final DeleteCoursePinPort deleteCoursePinPort;

    UnpinCourseService(LoadStepPort loadStepPort, UserCoursesPort userCoursesPort, LoadCoursePinPort loadCoursePinPort, DeleteCoursePinPort deleteCoursePinPort) {
        this.loadStepPort = loadStepPort;
        this.userCoursesPort = userCoursesPort;
        this.loadCoursePinPort = loadCoursePinPort;
        this.deleteCoursePinPort = deleteCoursePinPort;
    }

    @Override
    @Transactional
    public void unpin(UUID userId, UUID stepId, UUID pinId) {
        Accessible.step(loadStepPort, userId, stepId);
        CoursePin pin = loadCoursePinPort.load(pinId)
                .filter(p -> p.isOn(stepId))
                .filter(p -> userCoursesPort.isOwner(p.getCourseId(), userId))
                .orElseThrow(() -> new NotFoundException("Pin not found"));
        deleteCoursePinPort.delete(pin.getId());
    }
}
