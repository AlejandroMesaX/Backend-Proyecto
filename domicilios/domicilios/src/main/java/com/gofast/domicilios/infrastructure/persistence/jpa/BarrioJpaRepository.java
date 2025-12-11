package com.gofast.domicilios.infrastructure.persistence.jpa;

import com.gofast.domicilios.infrastructure.persistence.entity.BarrioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BarrioJpaRepository extends JpaRepository<BarrioEntity, Long> {
    Optional<BarrioEntity> findByNombre(String nombre);
}
