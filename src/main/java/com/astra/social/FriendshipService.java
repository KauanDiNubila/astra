package com.astra.social;

import com.astra.shared.CurrentUserProvider;
import com.astra.shared.exception.ConflictException;
import com.astra.shared.exception.NotFoundException;
import com.astra.social.dto.FriendshipResponse;
import com.astra.social.dto.SendFriendRequest;
import com.astra.user.User;
import com.astra.user.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository,
                             CurrentUserProvider currentUserProvider) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public FriendshipResponse sendRequest(SendFriendRequest request) {
        UUID me = currentUserProvider.currentUserId();
        User target = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        if (target.getId().equals(me)) {
            throw new ConflictException("Não é possível adicionar você mesmo");
        }

        Optional<Friendship> existing = friendshipRepository.findBetween(me, target.getId());
        if (existing.isPresent()) {
            Friendship f = existing.get();
            if (Friendship.ACCEPTED.equals(f.getStatus())) {
                throw new ConflictException("Vocês já são amigos");
            }
            if (f.getRequesterId().equals(me)) {
                throw new ConflictException("Convite já enviado");
            }
            // o outro já tinha te chamado — aceita direto, sem duplicar
            f.setStatus(Friendship.ACCEPTED);
            return toDto(f, me);
        }

        Friendship created = new Friendship(me, target.getId());
        // saveAndFlush pra @CreationTimestamp popular createdAt antes do toDto
        // (mesmo gotcha já documentado em SessionService)
        return toDto(friendshipRepository.saveAndFlush(created), me);
    }

    @Transactional
    public FriendshipResponse accept(UUID id) {
        UUID me = currentUserProvider.currentUserId();
        Friendship f = friendshipRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Convite não encontrado"));
        if (!f.getAddresseeId().equals(me)) {
            throw new NotFoundException("Convite não encontrado");
        }
        f.setStatus(Friendship.ACCEPTED);
        return toDto(f, me);
    }

    @Transactional
    public void remove(UUID id) {
        UUID me = currentUserProvider.currentUserId();
        Friendship f = friendshipRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Não encontrado"));
        if (!f.getRequesterId().equals(me) && !f.getAddresseeId().equals(me)) {
            throw new NotFoundException("Não encontrado");
        }
        friendshipRepository.delete(f);
    }

    @Transactional(readOnly = true)
    public List<FriendshipResponse> myFriends() {
        UUID me = currentUserProvider.currentUserId();
        return friendshipRepository.findAcceptedForUser(me).stream()
                .map(f -> toDto(f, me))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendshipResponse> pendingRequests() {
        UUID me = currentUserProvider.currentUserId();
        List<FriendshipResponse> received = friendshipRepository.findByAddresseeIdAndStatus(me, Friendship.PENDING)
                .stream().map(f -> toDto(f, me)).toList();
        List<FriendshipResponse> sent = friendshipRepository.findByRequesterIdAndStatus(me, Friendship.PENDING)
                .stream().map(f -> toDto(f, me)).toList();
        return Stream.concat(received.stream(), sent.stream()).toList();
    }

    @Transactional(readOnly = true)
    public List<UUID> friendIdsOf(UUID userId) {
        return friendshipRepository.findAcceptedForUser(userId).stream()
                .map(f -> f.otherUserId(userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean areFriends(UUID a, UUID b) {
        return friendshipRepository.findBetween(a, b)
                .map(f -> Friendship.ACCEPTED.equals(f.getStatus()))
                .orElse(false);
    }

    private FriendshipResponse toDto(Friendship f, UUID viewerId) {
        UUID otherId = f.otherUserId(viewerId);
        Optional<User> other = userRepository.findById(otherId);
        String otherName = other.map(User::getName).orElse("");
        String otherBio = other.map(User::getBio).orElse(null);
        boolean incoming = f.getAddresseeId().equals(viewerId);
        return new FriendshipResponse(f.getId(), otherId, otherName, otherBio, f.getStatus(), incoming, f.getCreatedAt());
    }
}
