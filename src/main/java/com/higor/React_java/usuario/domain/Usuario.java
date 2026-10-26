package com.higor.React_java.usuario.domain;

import java.util.UUID;

import lombok.Getter;

@Getter 
public class Usuario {
    
    
   
    private UUID id;

    private String email;
    
    private String senha;

    private String login;


    public Usuario(){

    }

    private Usuario(String email, String senha, String login){
        this.id = UUID.randomUUID();
        this.email = email;
        this.senha = senha;
        this.login = login;
    }

    public static Usuario criar(String email, String senha, String login){
        if (login == null || login.isBlank()) {                                                                              
                throw new IllegalArgumentException("Nome é obrigatório e não pode ser vazio.");                                
            }                                                                                                                  
                                                                                                                               
        if (email == null || email.isBlank()) {                                                                            
                throw new IllegalArgumentException("E-mail é obrigatório e não pode ser vazio.");                              
            }                                                                                                                  
                                                                                                                               
        if (!email.contains("@")) {                                                                                        
                throw new IllegalArgumentException("Formato de e-mail inválido.");                                             
            } 

        if (senha == null || senha.isBlank()) {                                                                              
                throw new IllegalArgumentException("Senha é obrigatória e não pode ser vazio.");                                
            } 
        return new Usuario(email,senha,login);
    }



}
