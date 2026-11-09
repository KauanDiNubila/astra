package com.astra.social.application.port.in;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public record SendFriendRequestCommand(UUID userId, String handle) {

    public static final String HANDLE_PATTERN = "^.{1,120}#\\d{4}$";
    private static final Pattern HANDLE = Pattern.compile(HANDLE_PATTERN);

    public SendFriendRequestCommand {
        Objects.requireNonNull(userId);
        if (handle == null || !HANDLE.matcher(handle).matches()) {
            throw new IllegalArgumentException("handle must look like name#0000");
        }
    }

    public String name() {
        return handle.substring(0, handle.lastIndexOf('#'));
    }

    public String tag() {
        return handle.substring(handle.lastIndexOf('#') + 1);
    }
}
