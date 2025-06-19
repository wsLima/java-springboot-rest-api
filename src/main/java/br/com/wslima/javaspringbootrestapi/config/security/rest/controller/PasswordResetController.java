package br.com.wslima.javaspringbootrestapi.config.security.rest.controller;

import br.com.wslima.javaspringbootrestapi.config.security.persistence.model.PasswordResetToken;
import br.com.wslima.javaspringbootrestapi.config.security.rest.dto.login.PasswordResetRequest;
import br.com.wslima.javaspringbootrestapi.config.security.rest.dto.login.PasswordResetTokenRequest;
import br.com.wslima.javaspringbootrestapi.config.security.rest.service.PasswordResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody PasswordResetRequest request) {
        PasswordResetToken token = passwordResetService.createPasswordResetToken(request.email());
        // TODO: Aqui você envia o e-mail. Exemplo de log:
        logger.info("Token de recuperação de senha gerado para {}: {}", request.email(), token.getToken());

        return ResponseEntity.ok("E-mail de recuperação enviado com sucesso.");
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetTokenRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok("Senha redefinida com sucesso.");
    }
}
