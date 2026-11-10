package com.astra.chat.adapter.out.persistence;

import com.astra.chat.application.port.out.AddGroupMemberPort;
import com.astra.chat.application.port.out.CheckGroupMemberPort;
import com.astra.chat.application.port.out.CreateGroupPort;
import com.astra.chat.application.port.out.LoadGroupPort;
import com.astra.chat.application.port.out.MarkGroupReadPort;
import com.astra.chat.domain.ChatGroup;
import com.astra.chat.domain.GroupMember;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class GroupPersistenceAdapter implements CreateGroupPort, LoadGroupPort, AddGroupMemberPort, CheckGroupMemberPort,
        MarkGroupReadPort {

    private final SpringDataGroupRepository groups;
    private final SpringDataGroupMemberRepository members;

    GroupPersistenceAdapter(SpringDataGroupRepository groups, SpringDataGroupMemberRepository members) {
        this.groups = groups;
        this.members = members;
    }

    private static GroupMember toDomain(GroupMemberJpaEntity e) {
        return new GroupMember(e.getUserId(), e.getJoinedAt(), e.getLastReadAt());
    }

    private static ChatGroup toDomain(GroupJpaEntity group, List<GroupMemberJpaEntity> groupMembers) {
        return ChatGroup.withId(group.getId(), group.getName(), group.getCreatedBy(),
                groupMembers.stream().map(GroupPersistenceAdapter::toDomain).toList());
    }

    @Override
    public ChatGroup create(ChatGroup group) {
        GroupJpaEntity saved = groups.save(new GroupJpaEntity(group.getName(), group.getCreatedBy()));
        List<GroupMemberJpaEntity> rows = members.saveAll(group.getMembers().stream()
                .map(m -> new GroupMemberJpaEntity(saved.getId(), m.userId(), m.joinedAt(), m.lastReadAt()))
                .toList());
        return toDomain(saved, rows);
    }

    @Override
    public Optional<ChatGroup> load(UUID groupId) {
        return groups.findById(groupId).map(group -> toDomain(group, members.findByGroupId(groupId)));
    }

    @Override
    public List<ChatGroup> listForMember(UUID userId) {
        List<UUID> groupIds = members.findGroupIdsByUserId(userId);
        if (groupIds.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<GroupMemberJpaEntity>> membersByGroup = members.findByGroupIdIn(groupIds).stream()
                .collect(Collectors.groupingBy(GroupMemberJpaEntity::getGroupId));
        Map<UUID, GroupJpaEntity> groupsById = groups.findAllById(groupIds).stream()
                .collect(Collectors.toMap(GroupJpaEntity::getId, g -> g));
        return groupIds.stream()
                .filter(groupsById::containsKey)
                .map(id -> toDomain(groupsById.get(id), membersByGroup.getOrDefault(id, List.of())))
                .toList();
    }

    @Override
    public Optional<String> nameOf(UUID groupId) {
        return groups.findNameById(groupId);
    }

    @Override
    public void add(UUID groupId, GroupMember member) {
        members.save(new GroupMemberJpaEntity(groupId, member.userId(), member.joinedAt(), member.lastReadAt()));
    }

    @Override
    public boolean isMember(UUID groupId, UUID userId) {
        return members.existsByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public List<UUID> memberIds(UUID groupId) {
        return members.findUserIdsByGroupId(groupId);
    }

    @Override
    public List<UUID> groupIdsOf(UUID userId) {
        return members.findGroupIdsByUserId(userId);
    }

    @Override
    public boolean markRead(UUID groupId, UUID userId, OffsetDateTime now) {
        return members.markRead(groupId, userId, now) > 0;
    }

}
