package com.astra.social.adapter.out.persistence;

import com.astra.social.application.port.out.CreateFriendshipPort;
import com.astra.social.application.port.out.DeleteFriendshipPort;
import com.astra.social.application.port.out.ListFriendshipsPort;
import com.astra.social.application.port.out.LoadFriendshipPort;
import com.astra.social.application.port.out.UpdateFriendshipStatusPort;
import com.astra.social.domain.Friendship;
import com.astra.social.domain.FriendshipStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class FriendshipPersistenceAdapter implements CreateFriendshipPort, LoadFriendshipPort, ListFriendshipsPort,
        UpdateFriendshipStatusPort, DeleteFriendshipPort {

    private final SpringDataFriendshipRepository repository;
    private final FriendshipMapper mapper;

    FriendshipPersistenceAdapter(SpringDataFriendshipRepository repository, FriendshipMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Friendship create(Friendship friendship) {
        return mapper.toDomain(repository.saveAndFlush(mapper.toJpaEntity(friendship)));
    }

    @Override
    public Optional<Friendship> load(UUID friendshipId) {
        return repository.findById(friendshipId).map(mapper::toDomain);
    }

    @Override
    public Optional<Friendship> loadBetween(UUID userId, UUID otherUserId) {
        return repository.findBetween(userId, otherUserId).map(mapper::toDomain);
    }

    @Override
    public List<Friendship> listAccepted(UUID userId) {
        return toDomain(repository.findByStatusForUser(userId, FriendshipStatus.ACCEPTED.name()));
    }

    @Override
    public List<Friendship> listPending(UUID userId) {
        return toDomain(repository.findByStatusForUser(userId, FriendshipStatus.PENDING.name()));
    }

    @Override
    public List<Friendship> listAll(UUID userId) {
        return toDomain(repository.findAllForUser(userId));
    }

    @Override
    public void updateStatus(Friendship friendship) {
        repository.findById(friendship.getId())
                .orElseThrow()
                .setStatus(friendship.getStatus().name());
    }

    @Override
    public void delete(UUID friendshipId) {
        repository.deleteById(friendshipId);
    }

    private List<Friendship> toDomain(List<FriendshipJpaEntity> entities) {
        return entities.stream().map(mapper::toDomain).toList();
    }
}
