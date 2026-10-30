package com.higor.React_java.service;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;


import com.higor.React_java.exception.RegraDeNegocioException;
import com.higor.React_java.model.dto.input.LoginRequest;
import com.higor.React_java.model.dto.output.LoginResponse;
import com.higor.React_java.mapper.UsuarioMapper;

import com.higor.React_java.repository.UsuarioRepository;

@Service 
public class AuthService {

    private final UsuarioRepository userRepository;
	private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository userRepository, PasswordEncoder passwordEncoder ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

    }

    public LoginResponse login(LoginRequest request){

        
        var usuario = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new RegraDeNegocioException("Credenciais inválidas"));

        if(!passwordEncoder.matches(request.senha(), usuario.getSenha())){
            throw new RegraDeNegocioException("Credenciais inválidas");
        }

        return UsuarioMapper.toLoginResponse(usuario);
        
    }
    
}

