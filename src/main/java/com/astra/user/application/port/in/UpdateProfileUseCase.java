package com.astra.user.application.port.in;

public interface UpdateProfileUseCase {

    UserResponse update(UpdateProfileCommand command);
}
