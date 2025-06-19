package br.com.wslima.javaspringbootrestapi.config.security.persistence.model;

import br.com.wslima.javaspringbootrestapi.persistence.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    public PasswordResetToken() {}

    public PasswordResetToken(User user, long durationMs) {
        this.user = user;
        this.token = UUID.randomUUID().toString();
        this.expiryDate = Instant.now().plusMillis(durationMs);
    }

}