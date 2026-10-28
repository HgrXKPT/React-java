package com.higor.React_java.usuario.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.higor.React_java.usuario.application.dto.LoginRequest;
import com.higor.React_java.usuario.application.dto.LoginResponse;
import com.higor.React_java.usuario.application.services.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/Login")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {
    

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody  LoginRequest request){

        var resultado = authService.login(request);

        return ResponseEntity.status(HttpStatus.OK).body(resultado);

    }
}
