package com.astra.chat.domain;

import com.astra.shared.exception.NotFoundException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ChatGroup {

    private final UUID id;
    private final String name;
    private final UUID createdBy;
    private final List<GroupMember> members;

    private ChatGroup(UUID id, String name, UUID createdBy, List<GroupMember> members) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.createdBy = createdBy;
        this.members = new ArrayList<>(members);
    }

    public static ChatGroup create(String name, UUID creatorId, List<UUID> friendIds, OffsetDateTime now) {
        List<GroupMember> members = new ArrayList<>();
        members.add(GroupMember.joining(creatorId, now));
        friendIds.stream()
                .distinct()
                .filter(id -> !id.equals(creatorId))
                .forEach(id -> members.add(GroupMember.joining(id, now)));
        return new ChatGroup(null, name.trim(), creatorId, members);
    }

    public static ChatGroup withId(UUID id, String name, UUID createdBy, List<GroupMember> members) {
        return new ChatGroup(Objects.requireNonNull(id), name, createdBy, members);
    }

    public List<GroupMember> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public List<UUID> memberIds() {
        return members.stream().map(GroupMember::userId).toList();
    }

    public boolean isMember(UUID userId) {
        return members.stream().anyMatch(m -> m.userId().equals(userId));
    }

    public boolean wasCreatedBy(UUID userId) {
        return userId.equals(createdBy);
    }

    public GroupMember addMember(UUID adderId, UUID newMemberId, OffsetDateTime now) {
        if (!isMember(adderId)) {
            throw new NotFoundException("Grupo não encontrado");
        }
        if (isMember(newMemberId)) {
            throw new AlreadyGroupMemberException();
        }
        GroupMember member = GroupMember.joining(newMemberId, now);
        members.add(member);
        return member;
    }
}
