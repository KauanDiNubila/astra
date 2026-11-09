package com.astra.social.adapter.out.user;

import com.astra.social.application.port.out.FindUserByHandlePort;
import com.astra.social.application.port.out.LoadUserSummariesPort;
import com.astra.user.entity.User;
import com.astra.user.repository.UserRepository;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class UserDirectoryAdapter implements FindUserByHandlePort, LoadUserSummariesPort {

    private final UserRepository userRepository;

    UserDirectoryAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<UUID> findIdByHandle(String name, String tag) {
        return userRepository.findByNameAndTag(name, tag).map(User::getId);
    }

    @Override
    public Map<UUID, UserSummary> loadByIds(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findNameBioByIdIn(userIds).stream()
                .collect(Collectors.toMap(UserRepository.NameBioView::getId,
                        v -> new UserSummary(v.getId(), v.getName(), v.getTag(), v.getBio(),
                                User.ROLE_ADMIN.equals(v.getRole()))));
    }
}
