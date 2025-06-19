package br.com.wslima.javaspringbootrestapi.config.security.rest.dto.login;

public record PasswordResetTokenRequest(String token, String newPassword) { }