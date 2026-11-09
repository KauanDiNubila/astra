package com.astra.stats.adapter.out.user;

import com.astra.stats.application.port.out.LoadRankedUsersPort;
import com.astra.user.entity.User;
import com.astra.user.repository.UserRepository;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class RankedUsersAdapter implements LoadRankedUsersPort {

    private final UserRepository userRepository;

    RankedUsersAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Map<UUID, RankedUser> loadByIds(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findNameBioByIdIn(userIds).stream()
                .collect(Collectors.toMap(UserRepository.NameBioView::getId,
                        v -> new RankedUser(v.getName(), User.isStaff(v.getRole()))));
    }
}
