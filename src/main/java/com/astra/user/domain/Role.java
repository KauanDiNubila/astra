package com.astra.user.domain;

import java.util.List;

public enum Role {
    USER(0),
    ADMIN(1),
    OWNER(2);

    private final int rank;

    Role(int rank) {
        this.rank = rank;
    }

    public boolean isStaff() {
        return rank >= ADMIN.rank;
    }

    public boolean isAbove(Role other) {
        return rank > other.rank;
    }

    public List<Role> withLowerRoles() {
        return switch (this) {
            case OWNER -> List.of(OWNER, ADMIN, USER);
            case ADMIN -> List.of(ADMIN, USER);
            case USER -> List.of(USER);
        };
    }
}
