package com.higor.React_java.model.dto.output;

import java.util.UUID;

import com.higor.React_java.model.domain.Usuario;

public record UsuarioOutputDTO(
    UUID id,
    String email,
    String login
) {

    public static UsuarioOutputDTO of(Usuario usuario) {
        return new UsuarioOutputDTO(usuario.getId(), usuario.getEmail(), usuario.getLogin());
    }

}
