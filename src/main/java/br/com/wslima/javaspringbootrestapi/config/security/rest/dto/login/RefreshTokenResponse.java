package br.com.wslima.javaspringbootrestapi.config.security.rest.dto.login;

public record RefreshTokenResponse(String accessToken, String refreshToken){
}