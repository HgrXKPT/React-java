package com.higor.React_java.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.higor.React_java.model.dto.input.LoginInputDTO;
import com.higor.React_java.model.dto.output.UsuarioOutputDTO;
import com.higor.React_java.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;

    @PostMapping("login")
    public ResponseEntity<UsuarioOutputDTO> login(@Valid @RequestBody  LoginInputDTO request){

        var resultado = authService.login(request);

        return ResponseEntity.status(HttpStatus.OK).body(UsuarioOutputDTO.of(resultado));

    }
}
