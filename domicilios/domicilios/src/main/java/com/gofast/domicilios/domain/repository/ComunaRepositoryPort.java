package com.gofast.domicilios.domain.repository;

import com.gofast.domicilios.domain.model.Comuna;
import org.springframework.data.domain.Page;
import java.util.Optional;
import java.util.List;

public interface ComunaRepositoryPort {
    Optional<Comuna> findByNumero(Integer numero);

    Optional<Comuna> findById(Long id);

    List<Comuna> findAll();

    Page<Comuna> findAll(int page, int size);

    boolean existsByNumero(Integer numero);

    Comuna save(Comuna comuna);
}
