package com.higor.React_java.service;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;


import com.higor.React_java.exception.RegraDeNegocioException;
import com.higor.React_java.model.domain.Usuario;
import com.higor.React_java.model.dto.input.LoginInputDTO;

import com.higor.React_java.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository userRepository;
	private final PasswordEncoder passwordEncoder;

    public Usuario login(LoginInputDTO request){

        
        var usuario = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new RegraDeNegocioException("Credenciais inválidas"));

        if(!passwordEncoder.matches(request.senha(), usuario.getSenha())){
            throw new RegraDeNegocioException("Credenciais inválidas");
        }

        return usuario;
        
    }
    
}

