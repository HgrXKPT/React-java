package com.higor.React_java.usuario.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginResponse(
    @NotBlank @Email String email,
    @NotBlank String login
) {
} 
