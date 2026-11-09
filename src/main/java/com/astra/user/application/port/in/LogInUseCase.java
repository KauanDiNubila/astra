package com.astra.user.application.port.in;

public interface LogInUseCase {

    UserResponse logIn(LogInCommand command);
}
