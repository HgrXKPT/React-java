package com.higor.React_java.usuario.application.mapper;

import com.higor.React_java.usuario.application.dto.CriarUsuarioResponse;
import com.higor.React_java.usuario.domain.Usuario;
import com.higor.React_java.usuario.infrastructure.entity.UsuarioEntity;

public class UsuarioMapper {

    public static UsuarioEntity toEntity(Usuario domain) {
        return new UsuarioEntity(
            domain.getId(),
            domain.getEmail(),
            domain.getSenha(),
            domain.getLogin()
        );
    }

    public static CriarUsuarioResponse toResponse(UsuarioEntity entity) {
        return new CriarUsuarioResponse(
            entity.getId(),
            entity.getEmail(),
            entity.getLogin()
        );
    }
}
