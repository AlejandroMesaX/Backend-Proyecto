package com.gofast.domicilios.domain.repository;

import com.gofast.domicilios.domain.model.Pedido;

import java.util.List;
import java.util.Optional;

public interface PedidoRepositoryPort {
    Pedido save(Pedido pedido);

    Optional<Pedido> findById(Long id);

    List<Pedido> findByClienteId(Long clienteId);

    List<Pedido> findByDomiciliarioId(Long domiciliarioId);

    List<Pedido> findAll();

}
