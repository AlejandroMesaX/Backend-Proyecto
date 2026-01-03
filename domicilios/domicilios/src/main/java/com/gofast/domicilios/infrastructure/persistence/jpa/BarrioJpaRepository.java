package com.gofast.domicilios.infrastructure.persistence.jpa;

import com.gofast.domicilios.infrastructure.persistence.entity.BarrioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.List;

public interface BarrioJpaRepository extends JpaRepository<BarrioEntity, Long>,
        JpaSpecificationExecutor<BarrioEntity>{
    Optional<BarrioEntity> findByNombre(String nombre);

    boolean existsByNombreIgnoreCaseAndActivoTrue(String nombre);

    Optional<BarrioEntity> findByNombreIgnoreCaseAndActivoTrue(String nombre);

    // ✅ Buscar por id solo si está activo (útil para operaciones)
    Optional<BarrioEntity> findByIdAndActivoTrue(Long id);
}
