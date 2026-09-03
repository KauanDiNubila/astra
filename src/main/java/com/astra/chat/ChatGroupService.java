package com.astra.chat;

import com.astra.chat.crypto.ChatEncryptionService;
import com.astra.chat.dto.CreateGroupRequest;
import com.astra.chat.dto.GroupConversationSummary;
import com.astra.chat.dto.GroupMemberResponse;
import com.astra.shared.CurrentUserProvider;
import com.astra.shared.exception.ConflictException;
import com.astra.shared.exception.NotFoundException;
import com.astra.social.FriendshipService;
import com.astra.user.UserRepository;
import com.astra.user.dto.AvatarData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// Criar grupo, adicionar membro, listar membros/conversas de grupo — fica
// separado de ChatService de propósito: mensagem (criptografia, anexo,
// reply) e "quem está no grupo" são responsabilidades diferentes.
@Service
public class ChatGroupService {

    private final ChatGroupRepository chatGroupRepository;
    private final ChatGroupMemberRepository chatGroupMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FriendshipService friendshipService;
    private final ChatEncryptionService chatEncryptionService;
    private final CurrentUserProvider currentUserProvider;

    public ChatGroupService(ChatGroupRepository chatGroupRepository,
            ChatGroupMemberRepository chatGroupMemberRepository, MessageRepository messageRepository,
            UserRepository userRepository, FriendshipService friendshipService,
            ChatEncryptionService chatEncryptionService, CurrentUserProvider currentUserProvider) {
        this.chatGroupRepository = chatGroupRepository;
        this.chatGroupMemberRepository = chatGroupMemberRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.friendshipService = friendshipService;
        this.chatEncryptionService = chatEncryptionService;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public GroupConversationSummary create(CreateGroupRequest request) {
        UUID me = currentUserProvider.currentUserId();
        List<UUID> memberIds = request.memberIds().stream().distinct().filter(id -> !id.equals(me)).toList();
        for (UUID memberId : memberIds) {
            if (!friendshipService.areFriends(me, memberId)) {
                throw new ConflictException("Só é possível adicionar amigos ao grupo");
            }
        }

        ChatGroup group = chatGroupRepository.save(new ChatGroup(request.name().trim(), me));

        List<ChatGroupMember> members = new ArrayList<>();
        members.add(new ChatGroupMember(group.getId(), me));
        for (UUID memberId : memberIds) {
            members.add(new ChatGroupMember(group.getId(), memberId));
        }
        chatGroupMemberRepository.saveAll(members);

        List<UUID> allMemberIds = members.stream().map(ChatGroupMember::getUserId).toList();
        return new GroupConversationSummary(group.getId(), group.getName(), memberNames(allMemberIds), null, null, 0L);
    }

    @Transactional
    public void addMember(UUID groupId, UUID userId) {
        UUID me = currentUserProvider.currentUserId();
        requireMember(groupId, me);
        if (!friendshipService.areFriends(me, userId)) {
            throw new ConflictException("Só é possível adicionar amigos ao grupo");
        }
        if (chatGroupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ConflictException("Usuário já está no grupo");
        }
        chatGroupMemberRepository.save(new ChatGroupMember(groupId, userId));
    }

    @Transactional
    public void updateAvatar(UUID groupId, MultipartFile file) {
        UUID me = currentUserProvider.currentUserId();
        requireMember(groupId, me);
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Grupo não encontrado"));

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ConflictException("Arquivo precisa ser uma imagem");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new ConflictException("Imagem precisa ter até 2MB");
        }
        try {
            group.setAvatar(file.getBytes());
            group.setAvatarContentType(contentType);
        } catch (IOException ex) {
            throw new ConflictException("Não foi possível ler o arquivo");
        }
    }

    // GET também passa por requireMember (diferente de UserService.avatar,
    // que é público entre autenticados) — o resto do grupo já esconde a
    // existência de quem não é membro, a foto segue a mesma regra.
    @Transactional(readOnly = true)
    public AvatarData avatar(UUID groupId) {
        UUID me = currentUserProvider.currentUserId();
        requireMember(groupId, me);
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Grupo não encontrado"));
        if (group.getAvatar() == null) {
            throw new NotFoundException("Grupo não encontrado");
        }
        return new AvatarData(group.getAvatar(), group.getAvatarContentType());
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponse> members(UUID groupId) {
        UUID me = currentUserProvider.currentUserId();
        requireMember(groupId, me);
        List<ChatGroupMember> members = chatGroupMemberRepository.findByGroupId(groupId);
        Map<UUID, UserRepository.NameBioView> usersById = nameBioMap(
                members.stream().map(ChatGroupMember::getUserId).toList());
        return members.stream()
                .map(m -> new GroupMemberResponse(m.getUserId(), nameOf(usersById, m.getUserId()), m.getJoinedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GroupConversationSummary> conversations() {
        UUID me = currentUserProvider.currentUserId();
        List<UUID> groupIds = chatGroupMemberRepository.findByUserId(me).stream()
                .map(ChatGroupMember::getGroupId)
                .toList();
        if (groupIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, ChatGroup> groupsById = chatGroupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(ChatGroup::getId, g -> g));
        Map<UUID, List<ChatGroupMember>> membersByGroup = chatGroupMemberRepository.findByGroupIdIn(groupIds).stream()
                .collect(Collectors.groupingBy(ChatGroupMember::getGroupId));
        Map<UUID, UserRepository.NameBioView> usersById = nameBioMap(membersByGroup.values().stream()
                .flatMap(List::stream)
                .map(ChatGroupMember::getUserId)
                .distinct()
                .toList());
        Map<UUID, Long> unreadByGroup = chatGroupMemberRepository.unreadCountsFor(me, groupIds).stream()
                .collect(Collectors.toMap(ChatGroupMemberRepository.UnreadByGroup::getGroupId,
                        ChatGroupMemberRepository.UnreadByGroup::getUnread));

        return groupIds.stream()
                .map(groupId -> {
                    ChatGroup group = groupsById.get(groupId);
                    List<String> names = membersByGroup.getOrDefault(groupId, List.of()).stream()
                            .map(m -> nameOf(usersById, m.getUserId()))
                            .toList();
                    long unread = unreadByGroup.getOrDefault(groupId, 0L);
                    return messageRepository.findFirstByGroupIdOrderByCreatedAtDesc(groupId)
                            .map(last -> new GroupConversationSummary(groupId, group.getName(), names,
                                    lastMessageText(last), last.getCreatedAt(), unread))
                            .orElseGet(() -> new GroupConversationSummary(groupId, group.getName(), names, null, null,
                                    unread));
                })
                .sorted(Comparator.comparing(GroupConversationSummary::lastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private String lastMessageText(Message last) {
        String content = chatEncryptionService.decrypt(last.getContent());
        return content != null ? content : "📷 Foto";
    }

    @Transactional(readOnly = true)
    public List<UUID> memberIds(UUID groupId) {
        return chatGroupMemberRepository.findUserIdsByGroupId(groupId);
    }

    void requireMember(UUID groupId, UUID userId) {
        if (!chatGroupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new NotFoundException("Grupo não encontrado");
        }
    }

    private List<String> memberNames(List<UUID> memberIds) {
        Map<UUID, UserRepository.NameBioView> usersById = nameBioMap(memberIds);
        return memberIds.stream().map(id -> nameOf(usersById, id)).toList();
    }

    private String nameOf(Map<UUID, UserRepository.NameBioView> usersById, UUID userId) {
        UserRepository.NameBioView user = usersById.get(userId);
        return user != null ? user.getName() : "";
    }

    private Map<UUID, UserRepository.NameBioView> nameBioMap(List<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userRepository.findNameBioByIdIn(ids).stream()
                .collect(Collectors.toMap(UserRepository.NameBioView::getId, v -> v));
    }
}
