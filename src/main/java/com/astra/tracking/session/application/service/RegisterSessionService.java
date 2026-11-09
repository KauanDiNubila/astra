package com.astra.tracking.session.application.service;

import com.astra.shared.exception.NotFoundException;
import com.astra.tracking.session.application.port.in.RegisterSessionCommand;
import com.astra.tracking.session.application.port.in.RegisterSessionUseCase;
import com.astra.tracking.session.application.port.in.SessionResponse;
import com.astra.tracking.session.application.port.out.CheckCategoryOwnerPort;
import com.astra.tracking.session.application.port.out.CheckCourseOwnerPort;
import com.astra.tracking.session.application.port.out.CreateSessionPort;
import com.astra.tracking.session.domain.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class RegisterSessionService implements RegisterSessionUseCase {

    private final CheckCategoryOwnerPort checkCategoryOwnerPort;
    private final CheckCourseOwnerPort checkCourseOwnerPort;
    private final CreateSessionPort createSessionPort;

    RegisterSessionService(CheckCategoryOwnerPort checkCategoryOwnerPort, CheckCourseOwnerPort checkCourseOwnerPort,
                           CreateSessionPort createSessionPort) {
        this.checkCategoryOwnerPort = checkCategoryOwnerPort;
        this.checkCourseOwnerPort = checkCourseOwnerPort;
        this.createSessionPort = createSessionPort;
    }

    @Override
    @Transactional
    public SessionResponse register(RegisterSessionCommand command) {
        if (!checkCategoryOwnerPort.isOwner(command.categoryId(), command.userId())) {
            throw new NotFoundException("Category not found");
        }
        if (command.courseId() != null && !checkCourseOwnerPort.isOwner(command.courseId(), command.userId())) {
            throw new NotFoundException("Course not found");
        }
        Session session = Session.start(command.userId(), command.categoryId(), command.courseId(),
                command.focusedMinutes(), command.startedAt(), command.note());
        return SessionResponse.from(createSessionPort.create(session));
    }
}
