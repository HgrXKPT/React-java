package com.higor.React_java.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.higor.React_java.model.domain.Usuario;

public interface UsuarioRepository extends  JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

	boolean existsByEmail(String email);

    
}
