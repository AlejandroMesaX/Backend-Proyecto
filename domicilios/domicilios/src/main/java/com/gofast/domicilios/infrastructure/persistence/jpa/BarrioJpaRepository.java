package com.gofast.domicilios.infrastructure.persistence.jpa;

import com.gofast.domicilios.infrastructure.persistence.entity.BarrioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface BarrioJpaRepository extends JpaRepository<BarrioEntity, Long> {
    Optional<BarrioEntity> findByNombre(String nombre);

    boolean existsByNombreIgnoreCaseAndActivoTrue(String nombre);

    Optional<BarrioEntity> findByNombreIgnoreCaseAndActivoTrue(String nombre);

    // ✅ Listar solo activos
    List<BarrioEntity> findAllByActivoTrue();

    // ✅ Buscar por id solo si está activo (útil para operaciones)
    Optional<BarrioEntity> findByIdAndActivoTrue(Long id);
}
