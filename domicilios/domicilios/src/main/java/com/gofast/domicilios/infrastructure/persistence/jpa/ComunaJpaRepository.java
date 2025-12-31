package com.gofast.domicilios.infrastructure.persistence.jpa;

import com.gofast.domicilios.infrastructure.persistence.entity.ComunaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComunaJpaRepository extends JpaRepository<ComunaEntity, Long> {
    Optional<ComunaEntity> findByNumero(Integer numero);
    boolean existsByNumero(Integer numero);
}
