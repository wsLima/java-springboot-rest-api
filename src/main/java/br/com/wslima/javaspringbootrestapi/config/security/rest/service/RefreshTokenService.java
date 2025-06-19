package br.com.wslima.javaspringbootrestapi.config.security.rest.service;

import br.com.wslima.javaspringbootrestapi.commons.exceptions.BadRequestException;
import br.com.wslima.javaspringbootrestapi.commons.exceptions.BusinessException;
import br.com.wslima.javaspringbootrestapi.config.security.persistence.model.RefreshToken;
import br.com.wslima.javaspringbootrestapi.config.security.persistence.repository.RefreshTokenRepository;
import br.com.wslima.javaspringbootrestapi.persistence.model.User;
import br.com.wslima.javaspringbootrestapi.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    private final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public RefreshToken createRefreshToken(User user) {
        if (user == null || user.getUuid() == null) {
            logger.error("Tentativa de criar refresh token para usuário inválido ou nulo.");
            throw new BusinessException("Usuário inválido para criação de refresh token.");
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        logger.info("Novo refresh token criado para o usuário: {}", user.getEmail());

        return refreshTokenRepository.save(refreshToken);
    }

    public boolean isTokenExpired(RefreshToken token) {
        if (token == null) {
            logger.error("Token nulo ao verificar expiração.");
            throw new BadRequestException("Token inválido para verificação de expiração.");
        }

        return token.getExpiryDate().isBefore(Instant.now());
    }

    @Transactional
    public void deleteByUser(User user) {
        if (user == null || user.getUuid() == null) {
            logger.error("Tentativa de deletar refresh tokens para usuário inválido ou nulo.");
            throw new BusinessException("Usuário inválido para deleção de refresh tokens.");
        }

        refreshTokenRepository.deleteByUser(user);
        logger.info("Refresh tokens deletados para o usuário: {}", user.getEmail());
    }

    public RefreshToken findByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.warn("Token de refresh vazio ou nulo recebido na busca.");
            throw new BadRequestException("O token de refresh é obrigatório.");
        }

        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    logger.warn("Token de refresh não encontrado no banco: {}", token);
                    return new BadRequestException("Refresh token inválido.");
                });
    }
}
