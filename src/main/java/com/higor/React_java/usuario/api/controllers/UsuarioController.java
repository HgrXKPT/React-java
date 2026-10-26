package com.higor.React_java.usuario.api.controllers;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.higor.React_java.usuario.application.dto.CriarUsuarioRequest;
import com.higor.React_java.usuario.application.dto.CriarUsuarioResponse;
import com.higor.React_java.usuario.application.services.UsuarioService;

@RestController
@RequestMapping("/Usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }

    @PostMapping("/Criar")
    public ResponseEntity<CriarUsuarioResponse> create(@RequestBody CriarUsuarioRequest request){

        var resultado = usuarioService.criar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        
    }
    
}
