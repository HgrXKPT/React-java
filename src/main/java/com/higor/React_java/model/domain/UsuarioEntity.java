package com.higor.React_java.model.domain;


import java.util.UUID;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true, length = 200) 
   
    private String email;

    @Column(name = "senha", nullable = false, length = 100) 
    private String senha;

    @Column(name = "login", nullable = false, length = 150)  
    private String login;

    protected UsuarioEntity(){

    };

    // Construtor utilitário para mapeamento                                                                               
        public UsuarioEntity(UUID id, String email, String senha, String login){
        this.id = id;
        this.email = email;
        this.senha = senha;
        this.login = login;
    }

}
