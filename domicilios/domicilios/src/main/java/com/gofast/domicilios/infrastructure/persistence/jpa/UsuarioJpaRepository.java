package com.gofast.domicilios.infrastructure.persistence.jpa;

import com.gofast.domicilios.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByEmail(String email);

    List<UsuarioEntity> findByNombreContainingIgnoreCase(String nombre);

    List<UsuarioEntity> findByRol(com.gofast.domicilios.domain.model.Rol rol);
}
