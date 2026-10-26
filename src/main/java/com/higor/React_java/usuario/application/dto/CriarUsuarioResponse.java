package com.higor.React_java.usuario.application.dto;

import java.util.UUID;

public record CriarUsuarioResponse(
    UUID id,
    String email,
    String login
) {
}
