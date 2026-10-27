package com.higor.React_java.usuario.application.services;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.higor.React_java.middleware.exception.RegraDeNegocioException;
import com.higor.React_java.usuario.application.dto.CriarUsuarioRequest;
import com.higor.React_java.usuario.application.dto.CriarUsuarioResponse;
import com.higor.React_java.usuario.application.mapper.UsuarioMapper;
import com.higor.React_java.usuario.domain.Usuario;
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
            throw new RegraDeNegocioException("E-mail já cadastrado");
         }
        
        Usuario usuario = Usuario.criar(
            request.email(), 
            passwordEncoder.encode(request.senha()), 
            request.login());
        

        var usuarioEntity = UsuarioMapper.toEntity(usuario);
        try{
            usuarioRepository.save(usuarioEntity);
        }catch(DataIntegrityViolationException ex){
            throw ex;
        }
        

        return UsuarioMapper.toResponse(usuarioEntity);
    }  
}
