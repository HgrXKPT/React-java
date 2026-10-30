package com.higor.React_java.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.higor.React_java.model.dto.input.UsuarioInputDTO;
import com.higor.React_java.model.dto.output.UsuarioOutputDTO;
import com.higor.React_java.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/Usuario")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/Criar")
    public ResponseEntity<UsuarioOutputDTO> create(@Valid @RequestBody UsuarioInputDTO request){

        var resultado = usuarioService.criar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioOutputDTO.of(resultado));
        
    }
    
}
