package com.financetracker.gmail.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "oauth_token")
public class OAuthToken {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID userId;

    @Column(name = "refresh_token")
    private String oAuthRefreshToken;

    @CreationTimestamp
    Instant createdAt;
    @UpdateTimestamp
    Instant updatedAt;

    public OAuthToken() {}

    public OAuthToken(UUID userId, String refreshToken) {
        this.userId = userId;
        this.oAuthRefreshToken = refreshToken;
    }
}
