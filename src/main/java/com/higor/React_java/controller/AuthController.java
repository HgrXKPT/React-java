package com.higor.React_java.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.higor.React_java.model.dto.input.LoginRequest;
import com.higor.React_java.model.dto.output.LoginResponse;
import com.higor.React_java.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/Login")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;

    @PostMapping("/")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody  LoginRequest request){

        var resultado = authService.login(request);

        return ResponseEntity.status(HttpStatus.OK).body(resultado);

    }
}
