package br.com.wslima.javaspringbootrestapi.config.security.persistence.repository;

import br.com.wslima.javaspringbootrestapi.config.security.persistence.model.RefreshToken;
import br.com.wslima.javaspringbootrestapi.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
