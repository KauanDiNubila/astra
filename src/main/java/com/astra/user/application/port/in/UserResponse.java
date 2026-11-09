package com.astra.user.application.port.in;

import com.astra.user.domain.User;
import java.util.UUID;

public record UserResponse(UUID id, String name, String email, String bio, String role, String tag,
                           boolean hasPassword, boolean termsAccepted) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getBio(), user.getRole().name(),
                user.getTag(), user.hasPassword(), user.hasAcceptedCurrentTerms());
    }
}
