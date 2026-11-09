package com.astra.roadmap.application.service;

import com.astra.roadmap.application.port.in.PinCourseCommand;
import com.astra.roadmap.application.port.in.PinCourseUseCase;
import com.astra.roadmap.application.port.in.PinResponse;
import com.astra.roadmap.application.port.out.LoadCoursePinPort;
import com.astra.roadmap.application.port.out.LoadStepPort;
import com.astra.roadmap.application.port.out.SaveCoursePinPort;
import com.astra.roadmap.application.port.out.UserCoursesPort;
import com.astra.roadmap.domain.CoursePin;
import com.astra.shared.exception.NotFoundException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PinCourseService implements PinCourseUseCase {

    private final LoadStepPort loadStepPort;
    private final UserCoursesPort userCoursesPort;
    private final LoadCoursePinPort loadCoursePinPort;
    private final SaveCoursePinPort saveCoursePinPort;

    PinCourseService(LoadStepPort loadStepPort, UserCoursesPort userCoursesPort, LoadCoursePinPort loadCoursePinPort, SaveCoursePinPort saveCoursePinPort) {
        this.loadStepPort = loadStepPort;
        this.userCoursesPort = userCoursesPort;
        this.loadCoursePinPort = loadCoursePinPort;
        this.saveCoursePinPort = saveCoursePinPort;
    }

    @Override
    @Transactional
    public PinResponse pin(PinCourseCommand command) {
        Accessible.step(loadStepPort, command.userId(), command.stepId());
        if (!userCoursesPort.isOwner(command.courseId(), command.userId())) {
            throw new NotFoundException("Course not found");
        }
        Optional<CoursePin> existing = loadCoursePinPort.loadOn(command.stepId(), command.courseId());
        if (existing.isPresent()) {
            CoursePin pin = existing.get();
            pin.change(command.status(), command.rating());
            saveCoursePinPort.update(pin);
            return PinResponse.from(pin);
        }
        CoursePin pin = CoursePin.pin(command.courseId(), command.stepId(), command.status(), command.rating());
        saveCoursePinPort.create(pin);
        return PinResponse.from(pin);
    }
}
