package com.astra.tracking.category.adapter.in.event;

import com.astra.shared.event.UserRegisteredEvent;
import com.astra.tracking.category.application.port.in.CreateDefaultCategoriesUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
class UserRegisteredListener {

    private final CreateDefaultCategoriesUseCase createDefaultCategoriesUseCase;

    UserRegisteredListener(CreateDefaultCategoriesUseCase createDefaultCategoriesUseCase) {
        this.createDefaultCategoriesUseCase = createDefaultCategoriesUseCase;
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void onUserRegistered(UserRegisteredEvent event) {
        createDefaultCategoriesUseCase.createDefaults(event.userId());
    }
}
