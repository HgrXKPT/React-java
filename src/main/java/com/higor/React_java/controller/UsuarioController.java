package com.higor.React_java.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.higor.React_java.model.dto.input.CriarUsuarioRequest;
import com.higor.React_java.model.dto.output.CriarUsuarioResponse;
import com.higor.React_java.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/Usuario")
@CrossOrigin(origins = "http://localhost:5173")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }

    @PostMapping("/Criar")
    public ResponseEntity<CriarUsuarioResponse> create(@Valid @RequestBody CriarUsuarioRequest request){

        var resultado = usuarioService.criar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        
    }
    
}
