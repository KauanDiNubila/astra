package com.astra.chat.application.service;

import com.astra.chat.application.port.in.ExportGroupsQuery;
import com.astra.chat.application.port.in.GetGroupInfoQuery;
import com.astra.chat.application.port.in.GetGroupPhotoQuery;
import com.astra.chat.application.port.in.GroupConversationSummary;
import com.astra.chat.application.port.in.GroupMemberResponse;
import com.astra.chat.application.port.in.ImageResponse;
import com.astra.chat.application.port.in.ListGroupConversationsQuery;
import com.astra.chat.application.port.in.ListGroupMembersQuery;
import com.astra.chat.application.port.out.CheckGroupMemberPort;
import com.astra.chat.application.port.out.CountUnreadPort;
import com.astra.chat.application.port.out.ListMessagesPort;
import com.astra.chat.application.port.out.LoadGroupPhotoPort;
import com.astra.chat.application.port.out.LoadGroupPort;
import com.astra.chat.application.port.out.LoadUserCardsPort;
import com.astra.chat.domain.ChatGroup;
import com.astra.chat.domain.GroupMember;
import com.astra.shared.ExportRow;
import com.astra.shared.exception.NotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class GroupQueryService implements ListGroupConversationsQuery, ListGroupMembersQuery, GetGroupPhotoQuery,
        GetGroupInfoQuery, ExportGroupsQuery {

    private final LoadGroupPort loadGroupPort;
    private final CheckGroupMemberPort checkGroupMemberPort;
    private final LoadGroupPhotoPort loadGroupPhotoPort;
    private final LoadUserCardsPort loadUserCardsPort;
    private final CountUnreadPort countUnreadPort;
    private final ListMessagesPort listMessagesPort;

    GroupQueryService(LoadGroupPort loadGroupPort, CheckGroupMemberPort checkGroupMemberPort,
                      LoadGroupPhotoPort loadGroupPhotoPort, LoadUserCardsPort loadUserCardsPort,
                      CountUnreadPort countUnreadPort, ListMessagesPort listMessagesPort) {
        this.loadGroupPort = loadGroupPort;
        this.checkGroupMemberPort = checkGroupMemberPort;
        this.loadGroupPhotoPort = loadGroupPhotoPort;
        this.loadUserCardsPort = loadUserCardsPort;
        this.countUnreadPort = countUnreadPort;
        this.listMessagesPort = listMessagesPort;
    }

    @Override
    public List<GroupConversationSummary> groups(UUID userId) {
        List<ChatGroup> groups = loadGroupPort.listForMember(userId);
        if (groups.isEmpty()) {
            return List.of();
        }
        MemberNames names = MemberNames.of(loadUserCardsPort,
                groups.stream().flatMap(g -> g.memberIds().stream()).distinct().toList());
        Map<UUID, Long> unread = countUnreadPort.inGroups(userId, groups.stream().map(ChatGroup::getId).toList());
        return groups.stream()
                .map(group -> {
                    List<String> memberNames = group.memberIds().stream().map(names::nameOf).toList();
                    long unreadCount = unread.getOrDefault(group.getId(), 0L);
                    return listMessagesPort.lastInGroup(group.getId())
                            .map(last -> new GroupConversationSummary(group.getId(), group.getName(), memberNames,
                                    last.summaryText(), last.getCreatedAt(), unreadCount))
                            .orElseGet(() -> new GroupConversationSummary(group.getId(), group.getName(),
                                    memberNames, null, null, unreadCount));
                })
                .sorted(Comparator.comparing(GroupConversationSummary::lastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public List<GroupMemberResponse> members(UUID userId, UUID groupId) {
        ChatGroup group = loadGroupPort.load(groupId)
                .filter(g -> g.isMember(userId))
                .orElseThrow(() -> new NotFoundException("Grupo não encontrado"));
        MemberNames names = MemberNames.of(loadUserCardsPort, group.memberIds());
        return group.getMembers().stream()
                .map(m -> new GroupMemberResponse(m.userId(), names.nameOf(m.userId()), m.joinedAt()))
                .toList();
    }

    @Override
    public ImageResponse photo(UUID userId, UUID groupId) {
        Access.requireMember(checkGroupMemberPort, groupId, userId);
        return loadGroupPhotoPort.load(groupId)
                .map(photo -> new ImageResponse(photo.bytes(), photo.contentType()))
                .orElseThrow(() -> new NotFoundException("Grupo não encontrado"));
    }

    @Override
    public List<UUID> memberIds(UUID groupId) {
        return checkGroupMemberPort.memberIds(groupId);
    }

    @Override
    public String name(UUID groupId) {
        return loadGroupPort.nameOf(groupId).orElseThrow(() -> new NotFoundException("Grupo não encontrado"));
    }

    @Override
    public List<Map<String, Object>> exportGroups(UUID userId) {
        return loadGroupPort.listForMember(userId).stream()
                .map(group -> ExportRow.of("groupId", group.getId(), "name", group.getName(),
                        "createdByMe", group.wasCreatedBy(userId),
                        "joinedAt", group.getMembers().stream()
                                .filter(m -> m.userId().equals(userId))
                                .map(GroupMember::joinedAt)
                                .findFirst()
                                .orElse(null)))
                .toList();
    }
}
