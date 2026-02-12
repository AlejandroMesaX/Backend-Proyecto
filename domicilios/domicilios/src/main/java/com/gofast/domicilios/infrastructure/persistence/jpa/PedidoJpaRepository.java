package com.gofast.domicilios.infrastructure.persistence.jpa;

import com.gofast.domicilios.domain.model.EstadoPedido;
import com.gofast.domicilios.infrastructure.persistence.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PedidoJpaRepository extends JpaRepository<PedidoEntity, Long>,
        JpaSpecificationExecutor<PedidoEntity> {
    List<PedidoEntity> findByClienteId(Long clienteId);

    List<PedidoEntity> findByDomiciliarioId(Long domiciliarioId);

    List<PedidoEntity> findByDomiciliarioIdAndEstadoOrderByIdDesc(Long domiciliarioId, EstadoPedido estado);
}
