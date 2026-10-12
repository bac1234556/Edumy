package com.edumy.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "RefreshTokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @Column(name = "Token", nullable = false)
    private String token;

    @Column(name = "Expires", nullable = false)
    private Instant expires;

    @Column(name = "Created", nullable = false)
    private Instant created = Instant.now();

    @Column(name = "Revoked")
    private Instant revoked;

    @Column(name = "ReplacedByToken")
    private String replacedByToken;

    protected RefreshToken() {
    }

    public RefreshToken(String token, Instant expires) {
        this.token = token;
        this.expires = expires;
    }

    void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public boolean isActive(Instant now) {
        return revoked == null && now.isBefore(expires);
    }

    public void revoke(Instant now, String replacement) {
        revoked = now;
        replacedByToken = replacement;
    }
}
