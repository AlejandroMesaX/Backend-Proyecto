package com.gofast.domicilios.domain.repository;

import com.gofast.domicilios.domain.model.Barrio;
import java.util.Optional;
import java.util.List;

public interface BarrioRepositoryPort {
    Optional<Barrio> findByNombre(String nombre);

    Optional<Barrio> findById(Long id);

    Barrio save(Barrio barrio);

    boolean existsActivoByNombre(String nombre);

    Optional<Barrio> findActivoByNombre(String nombre);

    // ✅ listar activos / listar todos
    List<Barrio> findAllActivos();
    List<Barrio> findAll();

    // ✅ soft delete / reactivate
    void desactivar(Long id);
    void reactivar(Long id);
}
