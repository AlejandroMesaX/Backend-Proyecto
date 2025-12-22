package com.gofast.domicilios.domain.repository;

import com.gofast.domicilios.domain.model.Barrio;
import java.util.Optional;

public interface BarrioRepositoryPort {
    Optional<Barrio> findByNombre(String nombre);

    Barrio save(Barrio barrio);

    boolean existsByNombre(String nombre);
}
