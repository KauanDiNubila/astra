package com.astra.user.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class UserAvatarJpaEntity {

    @Id
    private UUID id;

    private byte[] avatar;

    @Column(name = "avatar_content_type", length = 50)
    private String avatarContentType;
}
