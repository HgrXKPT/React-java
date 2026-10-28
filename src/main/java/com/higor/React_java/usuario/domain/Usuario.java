package com.higor.React_java.usuario.domain;

import java.util.UUID;

import com.higor.React_java.middleware.exception.RegraDeNegocioException;
import com.higor.React_java.usuario.ValueObjects.Email;

import lombok.Getter;

@Getter 
public class Usuario {
    
    private UUID id;

    private Email email;
    
    private String senha;

    private String login;

    public Usuario(){

    }

    private Usuario(UUID id, Email email, String senha, String login){
        validarLogin(login);
        validarSenha(senha);
        this.id = id;
        this.email = email;
        this.senha = senha;
        this.login = login;
    }

    public static Usuario criar(Email email, String senha, String login){
        UUID id = UUID.randomUUID();
        return new Usuario(id, email, senha, login);
    }

    public void atualizar(Email email, String senha, String login){
        if (email != null) {
            this.email = email;
        }
        
        if (senha != null && !senha.isBlank()) {
            this.senha = senha;
        }

        if (login != null && !login.isBlank()) {
            this.login = login;
        }
    }

    public static void validarSenha(String senha){
        if (senha == null || senha.isBlank()) {
            throw new RegraDeNegocioException("Senha é obrigatória e não pode ser vazio.");
        }
    }

    public static void validarLogin(String login){
        if (login == null || login.isBlank()) {
            throw new RegraDeNegocioException("Login é obrigatório e não pode ser vazio.");
        }

        if (login.length() > 150) {
            throw new RegraDeNegocioException("Login deve ter no máximo 150 caracteres");
        }
    }
}
