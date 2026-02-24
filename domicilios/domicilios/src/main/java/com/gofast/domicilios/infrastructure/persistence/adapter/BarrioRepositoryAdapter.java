package com.gofast.domicilios.infrastructure.persistence.adapter;

import com.gofast.domicilios.application.exception.NotFoundException;
import com.gofast.domicilios.domain.model.Barrio;
import com.gofast.domicilios.domain.repository.BarrioRepositoryPort;
import com.gofast.domicilios.infrastructure.persistence.entity.BarrioEntity;
import com.gofast.domicilios.infrastructure.persistence.entity.ComunaEntity;
import com.gofast.domicilios.infrastructure.persistence.jpa.BarrioJpaRepository;
import com.gofast.domicilios.infrastructure.persistence.jpa.ComunaJpaRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public Optional<Barrio> findById(Long id) {
        return barrioJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Barrio save(Barrio barrio) {
        ComunaEntity comuna = comunaJpaRepository.findByNumero(barrio.getComuna())
                .orElseThrow(() -> new NotFoundException(
                        "Comuna no encontrada: " + barrio.getComuna(),
                        "COMUNA_NOT_FOUND"
                ));

        BarrioEntity entity = new BarrioEntity();
        entity.setId(barrio.getId());
        entity.setNombre(barrio.getNombre());
        entity.setComuna(comuna);
        entity.setActivo(barrio.isActivo());

        BarrioEntity saved = barrioJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsActivoByNombre(String nombre) {
        return barrioJpaRepository.existsByNombreIgnoreCaseAndActivoTrue(nombre);
    }

    @Override
    public Optional<Barrio> findActivoByNombre(String nombre) {
        return barrioJpaRepository.findByNombreIgnoreCaseAndActivoTrue(nombre)
                .map(this::toDomain);
    }

    @Override
    public List<Barrio> findByFiltros(String nombre, Integer comunaNumero, Boolean activo) {

        Specification<BarrioEntity> spec = (root, query, cb) -> cb.conjunction();

        if (nombre != null && !nombre.isBlank()) {
            String like = "%" + nombre.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nombre")), like)
            );
        }

        if (comunaNumero != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("comuna").get("numero"), comunaNumero)
            );
        }

        if (activo != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("activo"), activo)
            );
        }

        return barrioJpaRepository.findAll(spec)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void desactivar(Long id) {
        BarrioEntity entity = barrioJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Barrio no encontrado: " + id,
                        "BARRIO_NOT_FOUND"
                ));

        entity.setActivo(false);
        barrioJpaRepository.save(entity);
    }

    @Override
    public void reactivar(Long id) {
        BarrioEntity entity = barrioJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Barrio no encontrado: " + id,
                        "BARRIO_NOT_FOUND"
                ));

        entity.setActivo(true);
        barrioJpaRepository.save(entity);
    }


    private Barrio toDomain(BarrioEntity entity) {
        Barrio barrio = new Barrio();
        barrio.setId(entity.getId());
        barrio.setNombre(entity.getNombre());
        barrio.setActivo(entity.isActivo());
        if (entity.getComuna() != null) {
            barrio.setComuna(entity.getComuna().getNumero());
        }
        return barrio;
    }
}
