package com.higor.React_java.model.dto.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarUsuarioRequest(
    @NotBlank @Email @Size  (min = 3, max = 255) String email,
    @NotBlank @Size  (min = 6, max = 100) String login,
    @NotBlank @Size  (min = 3, max = 64) String senha
) {
}
