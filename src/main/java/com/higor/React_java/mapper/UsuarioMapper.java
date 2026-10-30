package com.higor.React_java.mapper;

import com.higor.React_java.model.dto.output.CriarUsuarioResponse;
import com.higor.React_java.model.dto.output.LoginResponse;
import com.higor.React_java.usuario.domain.Usuario;
import com.higor.React_java.model.domain.UsuarioEntity;

public class UsuarioMapper {

    public static UsuarioEntity toEntity(Usuario domain) {
        return new UsuarioEntity(
            domain.getId(),
            domain.getEmail().email(),
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

    public static LoginResponse toLoginResponse(UsuarioEntity entity) {                                                        
        return new LoginResponse(                                                                                              
            entity.getId(),                                                                                                    
            entity.getEmail(),                                                                                                 
            entity.getLogin()                                                                                                  
        );                                                                                                                     
    }  
}
