package com.astra.social.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Friendship {

    private final UUID id;
    private final UUID requesterId;
    private final UUID addresseeId;
    private FriendshipStatus status;
    private final OffsetDateTime createdAt;

    private Friendship(UUID id, UUID requesterId, UUID addresseeId, FriendshipStatus status,
                       OffsetDateTime createdAt) {
        this.id = id;
        this.requesterId = Objects.requireNonNull(requesterId);
        this.addresseeId = Objects.requireNonNull(addresseeId);
        this.status = Objects.requireNonNull(status);
        this.createdAt = createdAt;
        if (requesterId.equals(addresseeId)) {
            throw new CannotBefriendYourselfException();
        }
    }

    public static Friendship request(UUID requesterId, UUID addresseeId) {
        return new Friendship(null, requesterId, addresseeId, FriendshipStatus.PENDING, null);
    }

    public static Friendship withId(UUID id, UUID requesterId, UUID addresseeId, FriendshipStatus status,
                                    OffsetDateTime createdAt) {
        return new Friendship(Objects.requireNonNull(id), requesterId, addresseeId, status, createdAt);
    }

    public boolean isAccepted() {
        return status == FriendshipStatus.ACCEPTED;
    }

    public boolean isRequester(UUID userId) {
        return requesterId.equals(userId);
    }

    public boolean isAddressee(UUID userId) {
        return addresseeId.equals(userId);
    }

    public boolean involves(UUID userId) {
        return isRequester(userId) || isAddressee(userId);
    }

    public UUID otherUserId(UUID viewerId) {
        return isRequester(viewerId) ? addresseeId : requesterId;
    }

    public void accept() {
        this.status = FriendshipStatus.ACCEPTED;
    }

    public void receiveRequestFrom(UUID senderId) {
        if (isAccepted()) {
            throw new AlreadyFriendsException();
        }
        if (isRequester(senderId)) {
            throw new FriendRequestAlreadySentException();
        }
        accept();
    }
}
