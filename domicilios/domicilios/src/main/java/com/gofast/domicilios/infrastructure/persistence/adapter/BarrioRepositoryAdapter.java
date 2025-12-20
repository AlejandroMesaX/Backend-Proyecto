package com.gofast.domicilios.infrastructure.persistence.adapter;

import com.gofast.domicilios.domain.model.Barrio;
import com.gofast.domicilios.domain.repository.BarrioRepositoryPort;
import com.gofast.domicilios.infrastructure.persistence.entity.BarrioEntity;
import com.gofast.domicilios.infrastructure.persistence.jpa.BarrioJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BarrioRepositoryAdapter implements BarrioRepositoryPort {
    private final BarrioJpaRepository barrioJpaRepository;

    public BarrioRepositoryAdapter(BarrioJpaRepository barrioJpaRepository) {
        this.barrioJpaRepository = barrioJpaRepository;
    }

    @Override
    public Optional<Barrio> findByNombre(String nombre) {
        return barrioJpaRepository.findByNombre(nombre)
                .map(this::toDomain);
    }

    private Barrio toDomain(BarrioEntity entity) {
        Barrio barrio = new Barrio();
        barrio.setId(entity.getId());
        barrio.setNombre(entity.getNombre());

        if (entity.getComuna() != null) {
            barrio.setComuna(entity.getComuna().getNumero());
        }

        return barrio;
    }
}
