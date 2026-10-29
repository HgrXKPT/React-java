package com.higor.React_java.usuario.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.higor.React_java.middleware.exception.RegraDeNegocioException;
import com.higor.React_java.usuario.valueObjects.Email;

public class UsuarioTest {

    @Test 
    void deveCriarUsuarioQuandoDadosForemValidos(){
        //Arrange
        Email email = new Email("teste123@gmail.com");
        String login = "teste123";
        String senha = "teste123";

        //Act
        var usuario = Usuario.criar(email, senha, login);

        //Assert

        assertEquals(senha, usuario.getSenha());
        assertEquals(login, usuario.getLogin());
        assertEquals(email, usuario.getEmail());
        assertNotNull(usuario.getId());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
   void deveLancarExcecaoQuandoLoginForNuloOuVazio(String login){
    //Arrange
    Email email = new Email("teste123@gmail.com");
    String senha = "teste123";

    //Act
    RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class,
        () -> Usuario.criar(email, senha, login));

    //Assert
    assertEquals("Login é obrigatório e não pode ser vazio.", exception.getMessage());
}
    @Test 
    void deveLancarExcecaoQuandoLoginTiverMaisDe150Caracteres(){
        //Arrange
        Email email = new Email("teste123@gmail.com");
        String login = "a".repeat(151);
        String senha = "teste123";
        //Act
         RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class,
        () -> Usuario.criar(email, senha, login));
        //Assert
            assertEquals("Login deve ter no máximo 150 caracteres", exception.getMessage());

    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveLancarExcecaoQuandoSenhaForNulaOuVazia(String senha){
         //Arrange
        Email email = new Email("teste123@gmail.com");
        String login = "teste123";
        
       
        //Act
         RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class,
        () -> Usuario.criar(email, senha, login));

         //Assert
            assertEquals("Senha é obrigatória e não pode ser vazio.", exception.getMessage());
        
    }

    @Test
    void deveLancarExcecaoQuandoEmailForNulo(){
        //Arrange
        Email email = null;
         String login = "teste123";
        String senha = "teste123";

        //Act

        RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class,
        () -> Usuario.criar(email, senha, login));

        //Assert

         assertEquals("O e-mail é obrigatório.", exception.getMessage());
    }

    @Test
    void deveCriarUsuarioQuandoLoginTiver150Caracteres(){
        //Arrange
        Email email = new Email("teste123@gmail.com");
        String login = "a".repeat(150);
        String senha = "teste123";
        //Act
         var usuario = Usuario.criar(email, senha, login);
        //Assert
        assertEquals(senha, usuario.getSenha());
        assertEquals(login, usuario.getLogin());
        assertEquals(email, usuario.getEmail());
        assertNotNull(usuario.getId());
    }
    
}
