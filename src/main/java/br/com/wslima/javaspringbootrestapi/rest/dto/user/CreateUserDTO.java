package br.com.wslima.javaspringbootrestapi.rest.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record CreateUserDTO(
        @NotBlank String name,
        @Email String email,
        @NotBlank String password
        ) {
}
