package com.gofast.domicilios.domain.repository;

import com.gofast.domicilios.domain.model.Comuna;
import java.util.Optional;

public interface ComunaRepositoryPort {
    Optional<Comuna> findByNumero(Integer numero);
}
