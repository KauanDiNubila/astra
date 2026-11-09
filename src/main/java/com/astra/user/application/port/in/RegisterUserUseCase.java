package com.astra.user.application.port.in;

public interface RegisterUserUseCase {

    UserResponse register(RegisterUserCommand command);
}
