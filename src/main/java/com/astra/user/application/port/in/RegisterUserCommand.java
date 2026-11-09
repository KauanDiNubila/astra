package com.astra.user.application.port.in;

public record RegisterUserCommand(String name, String email, String password, boolean acceptTerms) {

    public static final int NAME_MAX_LENGTH = 120;
    public static final int EMAIL_MAX_LENGTH = 180;
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 100;

    public RegisterUserCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (name.length() > NAME_MAX_LENGTH || name.contains("#")) {
            throw new IllegalArgumentException("name must have at most " + NAME_MAX_LENGTH + " characters and no '#'");
        }
        if (email == null || email.isBlank() || email.length() > EMAIL_MAX_LENGTH) {
            throw new IllegalArgumentException("email must not be blank and have at most " + EMAIL_MAX_LENGTH
                    + " characters");
        }
        if (password == null || password.length() < PASSWORD_MIN_LENGTH || password.length() > PASSWORD_MAX_LENGTH) {
            throw new IllegalArgumentException("password must have between " + PASSWORD_MIN_LENGTH + " and "
                    + PASSWORD_MAX_LENGTH + " characters");
        }
    }
}
