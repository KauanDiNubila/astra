package com.astra.user.application.port.out;

public interface CheckPasswordBreachPort {

    boolean isBreached(String password);
}
