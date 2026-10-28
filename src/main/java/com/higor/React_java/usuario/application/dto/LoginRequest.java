package com.higor.React_java.usuario.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank @Email @Size  (min = 3, max = 255) String email,
     @NotBlank @Size  (min = 3, max = 64) String senha
) {
}
