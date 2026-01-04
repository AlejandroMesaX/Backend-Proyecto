package com.gofast.domicilios.domain.repository;

import com.gofast.domicilios.domain.model.Direccion;

import java.util.List;
import java.util.Optional;

public interface DireccionRepositoryPort {

    Direccion save(Direccion direccion);

    Optional<Direccion> findById(Long id);

    List<Direccion> findByCliente(Long clienteId, Boolean activo);

    boolean existsByIdAndClienteId(Long id, Long clienteId);
}
