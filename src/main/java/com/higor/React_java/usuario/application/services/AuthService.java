package com.higor.React_java.usuario.application.services;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import com.higor.React_java.usuario.application.dto.LoginRequest;
import com.higor.React_java.usuario.application.dto.LoginResponse;
import com.higor.React_java.usuario.infrastructure.entity.UsuarioEntity;
import com.higor.React_java.usuario.infrastructure.repository.UsuarioRepository;

@Service 
public class AuthService {

    private final UsuarioRepository userRepository;
	private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository userRepository, PasswordEncoder passwordEncoder ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

    }

    public LoginResponse Login(LoginRequest request){

        return null;
    }
    
}
