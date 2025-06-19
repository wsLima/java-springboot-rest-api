package br.com.wslima.javaspringbootrestapi.config.security.rest.service;

import br.com.wslima.javaspringbootrestapi.commons.exceptions.BadRequestException;
import br.com.wslima.javaspringbootrestapi.commons.exceptions.BusinessException;
import br.com.wslima.javaspringbootrestapi.config.email.EmailService;
import br.com.wslima.javaspringbootrestapi.config.security.persistence.model.PasswordResetToken;
import br.com.wslima.javaspringbootrestapi.config.security.persistence.repository.PasswordResetTokenRepository;
import br.com.wslima.javaspringbootrestapi.config.security.rest.util.EmailTemplateBuilder;
import br.com.wslima.javaspringbootrestapi.persistence.model.User;
import br.com.wslima.javaspringbootrestapi.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PasswordResetService {

    @Value("${password.reset.expirationMs:3600000}")
    private Long expirationMs;

    @Value("${app.frontend.reset-password-url}")
    private String resetPasswordBaseUrl;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public PasswordResetToken createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("E-mail não encontrado."));

        tokenRepository.deleteByUser(user); // Remove tokens antigos

        PasswordResetToken token = new PasswordResetToken(user, expirationMs);
        tokenRepository.save(token);

        String resetUrl = resetPasswordBaseUrl + "?token=" + token.getToken();
        String emailBody = EmailTemplateBuilder.buildPasswordResetEmail(user.getName(), resetUrl);

        emailService.sendHtmlEmail(user.getEmail(), "Recuperação de senha", emailBody);

        logger.info("Token de recuperação enviado para {}", user.getEmail());

        return token;
    }

    @Transactional
    public void resetPassword(String tokenStr, String newPassword) {
        PasswordResetToken token = tokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new BadRequestException("Token inválido ou expirado."));

        if (token.getExpiryDate().isBefore(Instant.now())) {
            logger.warn("Token expirado: {}", tokenStr);
            throw new BadRequestException("Token expirado.");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(token);
        logger.info("Senha alterada com sucesso para usuário: {}", user.getEmail());
    }
}
