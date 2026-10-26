package com.higor.React_java.usuario.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CriarUsuarioRequest(
    @NotBlank @Email String email,
    @NotBlank String login,
    @NotBlank String senha
) {
}
