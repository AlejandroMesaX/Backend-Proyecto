package com.gofast.domicilios.infrastructure.persistence.jpa;

import com.gofast.domicilios.infrastructure.persistence.entity.DireccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DireccionJpaRepository extends JpaRepository<DireccionEntity, Long>{

    List<DireccionEntity> findByClienteId(Long clienteId);

    List<DireccionEntity> findByClienteIdAndActivo(Long clienteId, boolean activo);

    boolean existsByIdAndClienteId(Long id, Long clienteId);
}
