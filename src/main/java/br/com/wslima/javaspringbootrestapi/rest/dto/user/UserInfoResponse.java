package br.com.wslima.javaspringbootrestapi.rest.dto.user;

import java.util.Set;

public record UserInfoResponse(String name, String email, Set<String> roles) {}
