package com.gofast.domicilios.domain.repository;

import com.gofast.domicilios.domain.model.EstadoPedido;
import com.gofast.domicilios.domain.model.Pedido;
import com.gofast.domicilios.infrastructure.persistence.entity.PedidoEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PedidoRepositoryPort {
    Pedido save(Pedido pedido);

    Optional<Pedido> findById(Long id);

    List<Pedido> findByClienteId(Long clienteId);

    List<Pedido> findByDomiciliarioId(Long domiciliarioId);

    List<Pedido> findByFiltros(Long clienteId, Long domiciliarioId);

    List<Pedido> findByDomiciliarioIdYEstado(Long domiciliarioId, EstadoPedido estado);

    List<Pedido> findByClienteYFecha(Long clienteId, LocalDate desde, LocalDate hasta);

    List<Pedido> findEntregadosByDomiciliarioId(Long domiciliarioId);


    List<Pedido> findAll();

}
