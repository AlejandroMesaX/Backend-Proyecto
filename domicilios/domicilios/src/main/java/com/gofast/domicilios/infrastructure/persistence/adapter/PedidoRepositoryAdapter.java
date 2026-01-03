package com.gofast.domicilios.infrastructure.persistence.adapter;

import com.gofast.domicilios.domain.model.EstadoPedido;
import com.gofast.domicilios.domain.model.Pedido;
import com.gofast.domicilios.domain.repository.PedidoRepositoryPort;
import com.gofast.domicilios.infrastructure.persistence.entity.PedidoEntity;
import com.gofast.domicilios.infrastructure.persistence.jpa.PedidoJpaRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public List<Pedido> findByClienteYFecha(Long clienteId, LocalDate desde, LocalDate hasta) {

        Specification<PedidoEntity> spec = (root, query, cb) -> cb.conjunction();

        // ✅ solo del cliente
        spec = spec.and((root, query, cb) ->
                cb.equal(root.get("clienteId"), clienteId)
        );

        // ✅ rango fechas (si tu campo en entidad es LocalDateTime / Timestamp)
        if (desde != null) {
            LocalDateTime desdeDT = desde.atStartOfDay();
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("fechaCreacion"), desdeDT)
            );
        }

        if (hasta != null) {
            // hasta inclusive: < (hasta + 1 día) 00:00
            LocalDateTime hastaExclusivo = hasta.plusDays(1).atStartOfDay();
            spec = spec.and((root, query, cb) ->
                    cb.lessThan(root.get("fechaCreacion"), hastaExclusivo)
            );
        }

        return jpa.findAll(spec)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Pedido> findByDomiciliarioIdYEstado(Long domiciliarioId, EstadoPedido estado) {

        Specification<PedidoEntity> spec = (root, query, cb) -> cb.conjunction();

        spec = spec.and((root, query, cb) ->
                cb.equal(root.get("domiciliarioId"), domiciliarioId)
        );

        if (estado != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("estado"), estado)
            );
        }

        return jpa.findAll(spec)
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

    @Override
    public List<Pedido> findByFiltros(Long clienteId, Long domiciliarioId) {

        Specification<PedidoEntity> spec = (root, query, cb) -> cb.conjunction();

        if (clienteId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("clienteId"), clienteId)
            );
        }

        if (domiciliarioId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("domiciliarioId"), domiciliarioId)
            );
        }

        return jpa.findAll(spec)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private PedidoEntity toEntity(Pedido p) {
        PedidoEntity e = new PedidoEntity();
        e.setId(p.getId());
        e.setClienteId(p.getClienteId());
        e.setDomiciliarioId(p.getDomiciliarioId());
        e.setEstado(p.getEstado());
        e.setCostoServicio(p.getCostoServicio());
        e.setFechaCreacion(p.getFechaCreacion());

        e.setDireccionRecogida(p.getDireccionRecogida());
        e.setBarrioRecogida(p.getBarrioRecogida());
        e.setTelefonoContactoRecogida(p.getTelefonoContactoRecogida());

        e.setDireccionEntrega(p.getDireccionEntrega());
        e.setBarrioEntrega(p.getBarrioEntrega());
        e.setNombreQuienRecibe(p.getNombreQuienRecibe());
        e.setTelefonoQuienRecibe(p.getTelefonoQuienRecibe());

        return e;
    }

    private Pedido toDomain(PedidoEntity e) {
        Pedido p = new Pedido();
        p.setId(e.getId());
        p.setClienteId(e.getClienteId());
        p.setDomiciliarioId(e.getDomiciliarioId());
        p.setEstado(e.getEstado());
        p.setCostoServicio(e.getCostoServicio());
        p.setFechaCreacion(e.getFechaCreacion());

        p.setDireccionRecogida(e.getDireccionRecogida());
        p.setBarrioRecogida(e.getBarrioRecogida());
        p.setTelefonoContactoRecogida(e.getTelefonoContactoRecogida());

        p.setDireccionEntrega(e.getDireccionEntrega());
        p.setBarrioEntrega(e.getBarrioEntrega());
        p.setNombreQuienRecibe(e.getNombreQuienRecibe());
        p.setTelefonoQuienRecibe(e.getTelefonoQuienRecibe());
        return p;
    }
}
