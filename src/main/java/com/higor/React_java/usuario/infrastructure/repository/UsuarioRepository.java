package com.higor.React_java.usuario.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.higor.React_java.usuario.ValueObjects.Email;
import com.higor.React_java.usuario.infrastructure.entity.UsuarioEntity;

public interface UsuarioRepository extends  JpaRepository<UsuarioEntity, UUID> {
    
    Optional<UsuarioEntity> findByEmail(String email);

	boolean existsByEmail(String email);

    
}
