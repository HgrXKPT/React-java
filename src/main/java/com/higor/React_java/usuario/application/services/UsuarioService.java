package com.higor.React_java.usuario.application.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.higor.React_java.usuario.application.dto.CriarUsuarioRequest;
import com.higor.React_java.usuario.application.dto.CriarUsuarioResponse;
import com.higor.React_java.usuario.application.mapper.UsuarioMapper;
import com.higor.React_java.usuario.domain.Usuario;
import com.higor.React_java.usuario.infrastructure.entity.UsuarioEntity;
import com.higor.React_java.usuario.infrastructure.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CriarUsuarioResponse criar(CriarUsuarioRequest request) {

         if (usuarioRepository.existsByEmail(request.email())) {                                                                    
            throw new IllegalArgumentException("E-mail já cadastrado");
         }
        
        var hashSenha = passwordEncoder.encode(request.senha());

        Usuario usuario = Usuario.criar(request.email(), hashSenha, request.login());

        var usuarioEntity = UsuarioMapper.toEntity(usuario);
        usuarioRepository.save(usuarioEntity);

        return UsuarioMapper.toResponse(usuarioEntity);
    }  
}
