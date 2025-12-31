package com.gofast.domicilios.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import com.gofast.domicilios.domain.model.Comuna;
import com.gofast.domicilios.domain.repository.ComunaRepositoryPort;
import com.gofast.domicilios.infrastructure.persistence.entity.ComunaEntity;
import com.gofast.domicilios.infrastructure.persistence.jpa.ComunaJpaRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Component
public class ComunaRepositoryAdapter implements ComunaRepositoryPort {
    private final ComunaJpaRepository comunaJpaRepository;

    public ComunaRepositoryAdapter(ComunaJpaRepository comunaJpaRepository) {
        this.comunaJpaRepository = comunaJpaRepository;
    }

    @Override
    public Optional<Comuna> findByNumero(Integer numero) {
        return comunaJpaRepository.findByNumero(numero)
                .map(this::toDomain);
    }

    @Override
    public Optional<Comuna> findById(Long id) {
        return comunaJpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<Comuna> findAll() {
        return comunaJpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNumero(Integer numero) {
        return comunaJpaRepository.existsByNumero(numero);
    }

    @Override
    public Comuna save(Comuna comuna) {
        ComunaEntity entity = toEntity(comuna);
        ComunaEntity saved = comunaJpaRepository.save(entity);
        return toDomain(saved);
    }

    private Comuna toDomain(ComunaEntity entity) {
        Comuna comuna = new Comuna();
        comuna.setId(entity.getId());
        comuna.setNumero(entity.getNumero());
        comuna.setTarifaBase(entity.getTarifaBase());
        comuna.setRecargoPorSalto(entity.getRecargoPorSalto());
        return comuna;
    }

    private ComunaEntity toEntity(Comuna comuna) {

        ComunaEntity entity = new ComunaEntity();

        entity.setId(comuna.getId());
        entity.setNumero(comuna.getNumero());
        entity.setTarifaBase(comuna.getTarifaBase());
        entity.setRecargoPorSalto(comuna.getRecargoPorSalto());

        return entity;
    }
}
