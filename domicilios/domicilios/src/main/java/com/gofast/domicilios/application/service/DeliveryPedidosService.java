package com.gofast.domicilios.application.service;

import com.gofast.domicilios.application.dto.PedidoDTO;
import com.gofast.domicilios.application.exception.BadRequestException;
import com.gofast.domicilios.application.exception.ForbiddenException;
import com.gofast.domicilios.application.exception.NotFoundException;
import com.gofast.domicilios.domain.model.EstadoDelivery;
import com.gofast.domicilios.domain.model.EstadoPedido;
import com.gofast.domicilios.domain.model.Pedido;
import com.gofast.domicilios.domain.model.Usuario;
import com.gofast.domicilios.domain.repository.PedidoRepositoryPort;
import com.gofast.domicilios.domain.repository.UsuarioRepositoryPort;
import com.gofast.domicilios.infrastructure.persistence.entity.UsuarioEntity;
import com.gofast.domicilios.infrastructure.realtime.RealtimePublisher;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class DeliveryPedidosService {

    private final PedidoRepositoryPort pedidoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final DeliveryService deliveryService;
    private final RealtimePublisher realtimePublisher;
    private static final Logger log = LoggerFactory.getLogger(DeliveryPedidosService.class);

    public DeliveryPedidosService(
            PedidoRepositoryPort pedidoRepository,
            UsuarioRepositoryPort usuarioRepository,
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
                .orElseThrow(() -> {
                    log.warn(
                            "Pedido no encontrado al marcar recogido. pedidoId='{}'",
                            pedidoId);
                    return new NotFoundException(
                            "Pedido no encontrado",
                            "PEDIDO_NOT_FOUND");
                });

        Usuario d = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn(
                            "Domiciliario no encontrado en recogido. email='{}'",
                            email);
                    return new NotFoundException(
                            "Domiciliario no encontrado",
                            "DOMICILIARIO_NOT_FOUND");
                });

        if (!Objects.equals(p.getDomiciliarioId(), d.getId())) {
            log.warn(
                    "Domiciliario '{}' intentó recoger pedido '{}' que no le pertenece",
                    d.getId(), pedidoId);
            throw new ForbiddenException(
                    "No tienes permiso para recoger este pedido",
                    "PEDIDO_NO_ASIGNADO");
        }

        if (d.getEstadoDelivery() != EstadoDelivery.POR_RECOGER) {
            throw new BadRequestException(
                    "El domiciliario no está en estado válido para recoger",
                    "ESTADO_DELIVERY_INVALIDO",
                    "delivery");
        }

        p.setEstado(EstadoPedido.EN_CAMINO);
        pedidoRepository.save(p);

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
                .orElseThrow(() -> {
                    log.warn(
                            "Pedido no encontrado al marcar entregado. pedidoId='{}'",
                            pedidoId);
                    return new NotFoundException(
                            "Pedido no encontrado",
                            "PEDIDO_NOT_FOUND");
                });

        Usuario d = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn(
                            "Domiciliario no encontrado. email='{}'",
                            email);
                    return new NotFoundException(
                            "Domiciliario no encontrado",
                            "DOMICILIARIO_NOT_FOUND");
                });

        if (!Objects.equals(p.getDomiciliarioId(), d.getId())) {
            log.warn(
                    "Domiciliario '{}' intentó entregar pedido '{}' que no le pertenece",
                    d.getId(), pedidoId);
            throw new ForbiddenException(
                    "No tienes permiso para entregar este pedido",
                    "PEDIDO_NO_ASIGNADO");
        }

        if (d.getEstadoDelivery() != EstadoDelivery.POR_ENTREGAR) {
            throw new BadRequestException(
                    "El domiciliario no está en estado válido para entregar",
                    "ESTADO_DELIVERY_INVALIDO",
                    "delivery");
        }

        p.setEstado(EstadoPedido.ENTREGADO);
        pedidoRepository.save(p);

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
        dto.estado = p.getEstado().toString();
        dto.costoServicio = p.getCostoServicio();
        dto.fechaCreacion = p.getFechaCreacion().toString();
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