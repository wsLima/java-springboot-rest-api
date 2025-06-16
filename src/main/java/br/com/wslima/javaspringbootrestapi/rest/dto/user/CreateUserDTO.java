package br.com.wslima.javaspringbootrestapi.rest.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserDTO(
        @NotBlank String name,
        @Email String email,
        @NotBlank String password
//        @NotBlank Role role
        ) {
}
