package com.higor.React_java.usuario.valueObjects;

import java.util.Locale;

import com.higor.React_java.middleware.exception.RegraDeNegocioException;

public record Email(String email) {
 public Email {
        // 1. Valida se o e-mail é nulo ou vazio
        if (email == null || email.isBlank()) {
            throw new RegraDeNegocioException("O e-mail é obrigatório.");
        }

        // 2. Normaliza o valor
        email = email.trim().toLowerCase(Locale.ROOT);

        // 3. Valida o formato
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new RegraDeNegocioException("Formato de e-mail inválido.");
        }
    }
}
