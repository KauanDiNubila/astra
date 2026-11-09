package com.astra.user.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 180, unique = true)
    private String email;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(length = 80)
    private String bio;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "banned_at")
    private OffsetDateTime bannedAt;

    @Column(nullable = false, length = 4)
    private String tag;

    @Column(name = "terms_accepted_at")
    private OffsetDateTime termsAcceptedAt;

    @Column(name = "terms_version", length = 20)
    private String termsVersion;

    UserJpaEntity(String email) {
        this.email = email;
    }
}
