package com.higor.React_java.usuario.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.higor.React_java.usuario.infrastructure.entity.UsuarioEntity;

public interface UsuarioRepository extends  JpaRepository<UsuarioEntity, Long> {
    
    Optional<UsuarioEntity> findByEmail(String email);

	boolean existsByEmail(String email);

    
}
