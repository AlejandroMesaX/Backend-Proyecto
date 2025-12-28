package com.gofast.domicilios.infrastructure.persistence.jpa;

import com.gofast.domicilios.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long>,JpaSpecificationExecutor<UsuarioEntity> {
    Optional<UsuarioEntity> findByEmail(String email);
}
