package com.higor.React_java.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.higor.React_java.exception.RegraDeNegocioException;
import com.higor.React_java.usuario.valueObjects.Email;
import com.higor.React_java.model.dto.input.CriarUsuarioRequest;
import com.higor.React_java.model.dto.output.CriarUsuarioResponse;
import com.higor.React_java.mapper.UsuarioMapper;
import com.higor.React_java.usuario.domain.Usuario;
import com.higor.React_java.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CriarUsuarioResponse criar(CriarUsuarioRequest request) {

        Email email = new Email(request.email());
        
         if (usuarioRepository.existsByEmail(email.email())) {                                                                    
            throw new RegraDeNegocioException("E-mail já cadastrado");

         }
         validarSenha(request.senha());
        
        Usuario usuario = Usuario.criar(
            email,
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

    private static void validarSenha(String senha){
        if (senha == null || senha.isBlank()) {
            throw new RegraDeNegocioException("Senha é obrigatória e não pode ser vazio.");
        }
        if (senha.length() < 3 || senha.length() > 64) {
            throw new RegraDeNegocioException("Senha deve ter no minimo 3 caracteres e no maximo 64");
        }
    }

    
  
}
