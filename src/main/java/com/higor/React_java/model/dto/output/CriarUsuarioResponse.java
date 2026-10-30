package com.higor.React_java.model.dto.output;

import java.util.UUID;

public record CriarUsuarioResponse(
    UUID id,
    String email,
    String login
) {
}
