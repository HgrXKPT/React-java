package com.higor.React_java.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.higor.React_java.exception.RegraDeNegocioException;
import com.higor.React_java.model.domain.Usuario;
import com.higor.React_java.model.dto.input.UsuarioInputDTO;
import com.higor.React_java.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario criar(UsuarioInputDTO request) {

        String email = normalizarEmail(request.email());

         if (usuarioRepository.existsByEmail(email)) {
            throw new RegraDeNegocioException("E-mail já cadastrado");

         }
         validarSenha(request.senha());

        Usuario usuario = Usuario.builder()
            .id(UUID.randomUUID())
            .email(email)
            .senha(passwordEncoder.encode(request.senha()))
            .login(request.login())
            .build();


        try{
            usuarioRepository.save(usuario);
        }catch(DataIntegrityViolationException ex){
            throw ex;
        }


        return usuario;
    }

    private static String normalizarEmail(String email){
        if (email == null || email.isBlank()) {
            throw new RegraDeNegocioException("O e-mail é obrigatório.");
        }

        String normalizado = email.trim().toLowerCase(Locale.ROOT);

        if (!normalizado.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new RegraDeNegocioException("Formato de e-mail inválido.");
        }

        return normalizado;
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
