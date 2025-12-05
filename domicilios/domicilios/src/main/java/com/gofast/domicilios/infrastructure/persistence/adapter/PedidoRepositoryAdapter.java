package com.gofast.domicilios.infrastructure.persistence.adapter;

import com.gofast.domicilios.domain.model.Pedido;
import com.gofast.domicilios.domain.repository.PedidoRepositoryPort;
import com.gofast.domicilios.infrastructure.persistence.entity.PedidoEntity;
import com.gofast.domicilios.infrastructure.persistence.jpa.PedidoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PedidoRepositoryAdapter implements PedidoRepositoryPort {
    private final PedidoJpaRepository jpa;

    public PedidoRepositoryAdapter(PedidoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Pedido save(Pedido pedido) {
        PedidoEntity entity = toEntity(pedido);
        PedidoEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Pedido> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Pedido> findByClienteId(Long clienteId) {
        return jpa.findByClienteId(clienteId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Pedido> findByDomiciliarioId(Long domiciliarioId) {
        return jpa.findByDomiciliarioId(domiciliarioId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Pedido> findAll() {
        return jpa.findAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private PedidoEntity toEntity(Pedido p) {
        PedidoEntity e = new PedidoEntity();
        e.setId(p.getId());
        e.setClienteId(p.getClienteId());
        e.setDireccionId(p.getDireccionId());
        e.setDomiciliarioId(p.getDomiciliarioId());
        e.setEstado(p.getEstado());
        e.setTotal(p.getTotal());
        e.setFechaCreacion(p.getFechaCreacion());
        return e;
    }

    private Pedido toDomain(PedidoEntity e) {
        Pedido p = new Pedido();
        p.setId(e.getId());
        p.setClienteId(e.getClienteId());
        p.setDireccionId(e.getDireccionId());
        p.setDomiciliarioId(e.getDomiciliarioId());
        p.setEstado(e.getEstado());
        p.setTotal(e.getTotal());
        p.setFechaCreacion(e.getFechaCreacion());
        return p;
    }
}
