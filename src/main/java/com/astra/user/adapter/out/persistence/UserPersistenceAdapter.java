package com.astra.user.adapter.out.persistence;

import com.astra.user.application.port.in.AdminUserResponse;
import com.astra.user.application.port.in.AuthInfo;
import com.astra.user.application.port.in.UserResponse;
import com.astra.user.application.port.in.UserSummary;
import com.astra.user.application.port.out.CreateUserPort;
import com.astra.user.application.port.out.DeleteUserPort;
import com.astra.user.application.port.out.HandlePort;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.application.port.out.UpdateUserPort;
import com.astra.user.application.port.out.UserReadsPort;
import com.astra.user.domain.Role;
import com.astra.user.domain.User;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class UserPersistenceAdapter implements CreateUserPort, LoadUserPort, UpdateUserPort, DeleteUserPort, HandlePort,
        UserReadsPort {

    private final SpringDataUserRepository repository;
    private final UserMapper mapper;

    UserPersistenceAdapter(SpringDataUserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public User create(User user) {
        UserJpaEntity entity = new UserJpaEntity(user.getEmail());
        mapper.copy(user, entity);
        return mapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<User> load(UUID userId) {
        return repository.findById(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<User> loadByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public void update(User user) {
        mapper.copy(user, repository.findById(user.getId()).orElseThrow());
    }

    @Override
    public void delete(UUID userId) {
        repository.deleteById(userId);
    }

    @Override
    public boolean isTaken(String name, String tag) {
        return repository.existsByNameAndTag(name, tag);
    }

    @Override
    public Optional<UUID> findIdByHandle(String name, String tag) {
        return repository.findIdByNameAndTag(name, tag);
    }

    @Override
    public Optional<UserResponse> loadResponse(UUID userId) {
        return load(userId).map(UserResponse::from);
    }

    @Override
    public Optional<AuthInfo> loadAuthInfo(UUID userId) {
        return repository.findAuthInfoById(userId)
                .map(v -> new AuthInfo(v.getRole(),
                        Role.valueOf(v.getRole()).withLowerRoles().stream().map(Role::name).toList(),
                        v.getBannedAt() != null));
    }

    @Override
    public Map<UUID, UserSummary> loadSummaries(Collection<UUID> userIds) {
        return repository.findSummariesByIdIn(userIds).stream()
                .collect(Collectors.toMap(SpringDataUserRepository.SummaryView::getId,
                        v -> new UserSummary(v.getId(), v.getName(), v.getTag(), v.getBio(),
                                Role.valueOf(v.getRole()).isStaff())));
    }

    @Override
    public List<AdminUserResponse> listForAdmin() {
        return repository.findAllForAdmin().stream()
                .map(v -> new AdminUserResponse(v.getId(), v.getName(), v.getEmail(), v.getRole(),
                        v.getBannedAt() != null, v.getCreatedAt()))
                .toList();
    }
}
