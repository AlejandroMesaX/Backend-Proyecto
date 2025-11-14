package com.gofast.domicilios.infrastructure.repository;

import com.gofast.domicilios.infrastructure.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface JpaUsuarioRepository extends JpaRepository<UsuarioEntity , Long>{
    Optional<UsuarioEntity> findByEmail(String email);
}
