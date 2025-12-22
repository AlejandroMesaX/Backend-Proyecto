package com.gofast.domicilios.infrastructure.persistence.adapter;

import com.gofast.domicilios.domain.model.Barrio;
import com.gofast.domicilios.domain.repository.BarrioRepositoryPort;
import com.gofast.domicilios.infrastructure.persistence.entity.BarrioEntity;
import com.gofast.domicilios.infrastructure.persistence.entity.ComunaEntity;
import com.gofast.domicilios.infrastructure.persistence.jpa.BarrioJpaRepository;
import com.gofast.domicilios.infrastructure.persistence.jpa.ComunaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BarrioRepositoryAdapter implements BarrioRepositoryPort {
    private final BarrioJpaRepository barrioJpaRepository;
    private final ComunaJpaRepository comunaJpaRepository;

    public BarrioRepositoryAdapter(BarrioJpaRepository barrioJpaRepository, ComunaJpaRepository comunaJpaRepository) {
        this.barrioJpaRepository = barrioJpaRepository;
        this.comunaJpaRepository = comunaJpaRepository;
    }

    @Override
    public Optional<Barrio> findByNombre(String nombre) {
        return barrioJpaRepository.findByNombre(nombre)
                .map(this::toDomain);
    }


    @Override
    public boolean existsByNombre(String nombre) {
        return barrioJpaRepository.existsByNombreIgnoreCase(nombre);
    }

    @Override
    public Barrio save(Barrio barrio) {
        // Buscar comuna por número
        ComunaEntity comuna = comunaJpaRepository.findByNumero(barrio.getComuna())
                .orElseThrow(() -> new IllegalArgumentException("Comuna no encontrada: " + barrio.getComuna()));

        BarrioEntity entity = new BarrioEntity();
        entity.setNombre(barrio.getNombre());
        entity.setComuna(comuna);

        BarrioEntity saved = barrioJpaRepository.save(entity);
        return toDomain(saved);
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
