package com.gofast.domicilios.domain.repository;

import com.gofast.domicilios.domain.model.Barrio;
import org.springframework.data.domain.Page;
import java.util.Optional;
import java.util.List;

public interface BarrioRepositoryPort {
    Optional<Barrio> findByNombre(String nombre);

    Optional<Barrio> findById(Long id);

    List<Barrio> findAllActivos();

    Barrio save(Barrio barrio);

    boolean existsActivoByNombre(String nombre);

    Optional<Barrio> findActivoByNombre(String nombre);

    List<Barrio> findByFiltros(String nombre, Integer comunaNumero, Boolean activo);

    Page<Barrio> findByFiltros(String nombre, Integer comunaNumero, Boolean activo, int page, int size);

    void desactivar(Long id);

    void reactivar(Long id);
}
