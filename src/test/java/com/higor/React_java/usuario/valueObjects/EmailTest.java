package com.higor.React_java.usuario.valueObjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.higor.React_java.middleware.exception.RegraDeNegocioException;
import com.higor.React_java.usuario.valueObjects.Email;



public class EmailTest {

    @Test 
    void deveCriarENormalizarEmailValido() {                                                                               
            // Arrange: define a entrada com maiúsculas e espaços nas pontas                                                   
            String entrada = "  User@Exemplo.COM  ";                                                                           
                                                                                                                               
            // Act: cria a instância                                                                                           
            Email email = new Email(entrada);                                                                                  
                                                                                                                               
            // Assert: garante que o getter do record retorna o valor limpo                                                    
            assertEquals("user@exemplo.com", email.email());
        }
    @Test
    void deveFalharDadoEmailInvalido(){
        //Arrange
        RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class, 
            () -> new Email("email-falho")
        );

        //Assert

        assertEquals("Formato de e-mail inválido.", exception.getMessage());    ;
    
    }   

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveLancarExcecaoQuandoEmailForNuloOuVazio(String entrada){
        //Act
        RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class,
            () -> new Email(entrada));

        //Assert
        assertEquals("O e-mail é obrigatório.", exception.getMessage());
    }
}
