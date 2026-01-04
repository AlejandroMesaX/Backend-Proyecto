package com.gofast.domicilios.infrastructure.persistence.adapter;


import com.gofast.domicilios.domain.model.Direccion;
import com.gofast.domicilios.domain.repository.DireccionRepositoryPort;
import com.gofast.domicilios.infrastructure.persistence.entity.BarrioEntity;
import com.gofast.domicilios.infrastructure.persistence.entity.DireccionEntity;
import com.gofast.domicilios.infrastructure.persistence.jpa.DireccionJpaRepository;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DireccionRepositoryAdapter implements DireccionRepositoryPort {
    private final DireccionJpaRepository direccionJpaRepository;

    public DireccionRepositoryAdapter(DireccionJpaRepository direccionJpaRepository) {
        this.direccionJpaRepository = direccionJpaRepository;
    }

    @Override
    public Direccion save(Direccion direccion) {
        DireccionEntity entity = toEntity(direccion);
        DireccionEntity saved = direccionJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Direccion> findById(Long id) {
        return direccionJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Direccion> findByCliente(Long clienteId, Boolean activo) {
        List<DireccionEntity> list;
        if (activo == null) {
            list = direccionJpaRepository.findByClienteId(clienteId);
        } else {
            list = direccionJpaRepository.findByClienteIdAndActivo(clienteId, activo);
        }
        return list.stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByIdAndClienteId(Long id, Long clienteId) {
        return direccionJpaRepository.existsByIdAndClienteId(id, clienteId);
    }

    private DireccionEntity toEntity(Direccion d) {
        DireccionEntity e = new DireccionEntity();
        e.setId(d.getId());
        e.setClienteId(d.getClienteId());

        BarrioEntity barrioRef = new BarrioEntity();
        barrioRef.setId(d.getBarrioId());
        e.setBarrio(barrioRef);

        e.setDireccionRecogida(d.getDireccionRecogida());
        e.setTelefonoContacto(d.getTelefonoContacto());
        e.setActivo(Boolean.TRUE.equals(d.getActivo()));

        return e;
    }

    private Direccion toDomain(DireccionEntity e) {
        Direccion d = new Direccion();
        d.setId(e.getId());
        d.setClienteId(e.getClienteId());
        d.setBarrioId(e.getBarrio().getId());
        d.setDireccionRecogida(e.getDireccionRecogida());
        d.setTelefonoContacto(e.getTelefonoContacto());
        d.setActivo(e.isActivo());
        return d;
    }
}
