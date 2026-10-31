package com.higor.React_java.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.higor.React_java.exception.RegraDeNegocioException;
import com.higor.React_java.model.dto.input.UsuarioInputDTO;
import com.higor.React_java.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    @Test
    void deveCriarUsuarioQuandoDadosForemValidos() {
        when(passwordEncoder.encode("teste123")).thenReturn("hash");

        var usuario = service.criar(new UsuarioInputDTO("teste123@gmail.com", "teste123", "teste123"));

        assertThat(usuario.getEmail()).isEqualTo("teste123@gmail.com");
        assertThat(usuario.getLogin()).isEqualTo("teste123");
        assertThat(usuario.getSenha()).isEqualTo("hash");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveNormalizarEmailAntesDeChecarDuplicidade() {
        when(passwordEncoder.encode("teste123")).thenReturn("hash");

        var usuario = service.criar(new UsuarioInputDTO("  User@Exemplo.COM  ", "teste123", "teste123"));

        verify(usuarioRepository).existsByEmail("user@exemplo.com");
        assertThat(usuario.getEmail()).isEqualTo("user@exemplo.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"email-falho", "usuario@", "@exemplo.com"})
    void deveLancarExcecaoQuandoFormatoDoEmailForInvalido(String email) {
        assertThatThrownBy(() -> service.criar(new UsuarioInputDTO(email, "teste123", "teste123")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Formato de e-mail inválido.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveLancarExcecaoQuandoEmailForNuloOuVazio(String email) {
        assertThatThrownBy(() -> service.criar(new UsuarioInputDTO(email, "teste123", "teste123")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("O e-mail é obrigatório.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveLancarExcecaoQuandoSenhaForNulaOuVazia(String senha) {
        assertThatThrownBy(() -> service.criar(new UsuarioInputDTO("teste123@gmail.com", "teste123", senha)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Senha é obrigatória e não pode ser vazio.");
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaEstiverCadastrado() {
        when(usuarioRepository.existsByEmail("teste123@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new UsuarioInputDTO("teste123@gmail.com", "teste123", "teste123")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("E-mail já cadastrado");
    }

}
