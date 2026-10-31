package com.higor.React_java.exception;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.higor.React_java.exception.ConflitoDeDadosException;
import com.higor.React_java.exception.RegraDeNegocioException;

@RestControllerAdvice                                                                                                      
    public class GlobalExceptionHandler {                                                                                      
                                                                                                                               
        @ExceptionHandler(RegraDeNegocioException.class)                                                                  
        public ResponseEntity<ErroResponseDto> handleNegocio(RegraDeNegocioException ex) {                                
            
            HttpStatus status = HttpStatus.BAD_REQUEST;
            ErroResponseDto erro = new  ErroResponseDto(status.value(), ex.getMessage(), Instant.now());
            return ResponseEntity.status(status).body(erro);                                                            
                                                               
        } 
        
        @ExceptionHandler(ConflitoDeDadosException.class)
        public ResponseEntity<ErroResponseDto> handleNegocio(ConflitoDeDadosException ex) {                                
            
            HttpStatus status = HttpStatus.CONFLICT;
            ErroResponseDto erro = new  ErroResponseDto(status.value(), ex.getMessage(), Instant.now());
            return ResponseEntity.status(status).body(erro);                                                            
                                                               
        } 
                                                                                                                               
        //@ExceptionHandler(MethodArgumentNotValidException.class)                                                               
        //public ResponseEntity<ErroResponseDto> handleValidacao(MethodArgumentNotValidException ex) {                           
            // Itera sobre ex.getBindingResult().getFieldErrors() para mapear os campos inválidos                              
        //}                                                                                                                      
    } 
