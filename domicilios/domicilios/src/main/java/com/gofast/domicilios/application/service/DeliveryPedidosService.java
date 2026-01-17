package com.gofast.domicilios.application.service;

import com.gofast.domicilios.application.dto.PedidoDTO;
import com.gofast.domicilios.domain.model.EstadoDelivery;
import com.gofast.domicilios.domain.model.EstadoPedido;
import com.gofast.domicilios.domain.model.Pedido;
import com.gofast.domicilios.domain.model.Usuario;
import com.gofast.domicilios.domain.repository.PedidoRepositoryPort;
import com.gofast.domicilios.domain.repository.UsuarioRepositoryPort;
import com.gofast.domicilios.infrastructure.persistence.entity.UsuarioEntity;
import com.gofast.domicilios.infrastructure.persistence.jpa.UsuarioJpaRepository;
import com.gofast.domicilios.infrastructure.realtime.RealtimePublisher;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class DeliveryPedidosService {

    private final PedidoRepositoryPort pedidoRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final DeliveryService deliveryService;
    private final RealtimePublisher realtimePublisher;

    public DeliveryPedidosService(
            PedidoRepositoryPort pedidoRepository,
            UsuarioJpaRepository usuarioRepository,
            DeliveryService deliveryService,
            RealtimePublisher realtimePublisher
    ) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.deliveryService = deliveryService;
        this.realtimePublisher = realtimePublisher;
    }

    @Transactional
    public void marcarRecogido(Long pedidoId, String email) {
        Pedido p = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no existe"));

        UsuarioEntity d = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Domiciliario no existe"));

        // ✅ debe ser el dueño del pedido
        if (!Objects.equals(p.getDomiciliarioId(), d.getId())) {
            throw new RuntimeException("Este pedido no te pertenece");
        }

        // ✅ transición válida
        if (d.getEstadoDelivery() != EstadoDelivery.POR_RECOGER) {
            throw new RuntimeException("Estado del domiciliario inválido para recoger");
        }

        // Pedido a EN_CAMINO
        p.setEstado(EstadoPedido.EN_CAMINO);
        pedidoRepository.save(p);

        // Domiciliario a POR_ENTREGAR
        d.setEstadoDelivery(EstadoDelivery.POR_ENTREGAR);
        usuarioRepository.save(d);

        PedidoDTO dto = toDTO(p);
        realtimePublisher.pedidoActualizado(dto);
        realtimePublisher.deliveryActualizado(deliveryService.toDto(d));
        realtimePublisher.pedidoParaDelivery(d.getId(), dto);
    }

    @Transactional
    public void marcarEntregado(Long pedidoId, String email) {
        Pedido p = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no existe"));

        UsuarioEntity d = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Domiciliario no existe"));

        if (!Objects.equals(p.getDomiciliarioId(), d.getId())) {
            throw new RuntimeException("Este pedido no te pertenece");
        }

        if (d.getEstadoDelivery() != EstadoDelivery.POR_ENTREGAR) {
            throw new RuntimeException("Estado del domiciliario inválido para entregar");
        }

        // Pedido terminado
        p.setEstado(EstadoPedido.ENTREGADO);
        pedidoRepository.save(p);

        // Domiciliario vuelve a DISPONIBLE (entra de nuevo a la cola)
        d.setEstadoDelivery(EstadoDelivery.DISPONIBLE);
        d.setDisponibleDesde(LocalDateTime.now());
        usuarioRepository.save(d);

        PedidoDTO dto = toDTO(p);
        realtimePublisher.pedidoActualizado(dto);
        realtimePublisher.deliveryActualizado(deliveryService.toDto(d));
    }

    private PedidoDTO toDTO(Pedido p) {
        PedidoDTO dto = new PedidoDTO();
        dto.id = p.getId();
        dto.clienteId = p.getClienteId();
        dto.domiciliarioId = p.getDomiciliarioId();
        dto.estado = p.getEstado().toString(); // ajusta si es enum
        dto.costoServicio = p.getCostoServicio();
        dto.fechaCreacion = p.getFechaCreacion().toString(); // ajusta
        dto.direccionRecogida = p.getDireccionRecogida();
        dto.barrioRecogida = p.getBarrioRecogida();
        dto.telefonoContactoRecogida = p.getTelefonoContactoRecogida();
        dto.direccionEntrega = p.getDireccionEntrega();
        dto.barrioEntrega = p.getBarrioEntrega();
        dto.nombreQuienRecibe = p.getNombreQuienRecibe();
        dto.telefonoQuienRecibe = p.getTelefonoQuienRecibe();
        return dto;
    }
}