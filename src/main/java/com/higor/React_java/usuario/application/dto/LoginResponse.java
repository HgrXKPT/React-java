package com.higor.React_java.usuario.application.dto;

import java.util.UUID;

public record LoginResponse(
    UUID id,
    String email,
    String login
) {
} 
