package br.com.wslima.javaspringbootrestapi.config.security.rest.dto.login;

public record LoginResponse(String accessToken, String refreshToken){
}