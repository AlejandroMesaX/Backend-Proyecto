package com.gofast.domicilios.infrastructure.persistence.jpa;

import com.gofast.domicilios.infrastructure.persistence.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoJpaRepository extends JpaRepository<PedidoEntity, Long> {
    List<PedidoEntity> findByClienteId(Long clienteId);

    List<PedidoEntity> findByDomiciliarioId(Long domiciliarioId);
}
