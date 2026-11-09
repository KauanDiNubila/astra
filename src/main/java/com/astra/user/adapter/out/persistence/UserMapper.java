package com.astra.user.adapter.out.persistence;

import com.astra.user.domain.Role;
import com.astra.user.domain.User;
import org.springframework.stereotype.Component;

@Component
class UserMapper {

    User toDomain(UserJpaEntity entity) {
        return User.withId(entity.getId(), entity.getName(), entity.getEmail(), entity.getPasswordHash(),
                entity.getBio(), Role.valueOf(entity.getRole()), entity.getBannedAt(), entity.getTag(),
                entity.getTermsAcceptedAt(), entity.getTermsVersion(), entity.getCreatedAt());
    }

    void copy(User user, UserJpaEntity entity) {
        entity.setName(user.getName());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setBio(user.getBio());
        entity.setRole(user.getRole().name());
        entity.setBannedAt(user.getBannedAt());
        entity.setTag(user.getTag());
        entity.setTermsAcceptedAt(user.getTermsAcceptedAt());
        entity.setTermsVersion(user.getTermsVersion());
    }
}
