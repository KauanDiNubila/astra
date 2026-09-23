package com.astra.chat.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.astra.chat.entity.ChatGroup;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, UUID> {
}
