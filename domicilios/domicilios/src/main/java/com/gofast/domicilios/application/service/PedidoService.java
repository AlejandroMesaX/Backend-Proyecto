package com.gofast.domicilios.application.service;

import com.gofast.domicilios.application.dto.*;
import com.gofast.domicilios.application.exception.BadRequestException;
import com.gofast.domicilios.application.exception.ForbiddenException;
import com.gofast.domicilios.application.exception.NotFoundException;
import com.gofast.domicilios.domain.model.EstadoPedido;
import com.gofast.domicilios.domain.model.Pedido;
import com.gofast.domicilios.domain.repository.PedidoRepositoryPort;
import com.gofast.domicilios.domain.service.TarifaDomicilioService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepositoryPort pedidoRepository;
    private final TarifaDomicilioService tarifaDomicilioService;

    private static final Set<EstadoPedido> PERMITIR_EN_CAMINO_DESDE =
            EnumSet.of(EstadoPedido.ASIGNADO);
    private static final Set<EstadoPedido> PERMITIR_ENTREGADO_DESDE =
            EnumSet.of(EstadoPedido.EN_CAMINO);
    private static final Set<EstadoPedido> PERMITIR_CANCELADO_DESDE =
            EnumSet.of(EstadoPedido.CREADO, EstadoPedido.ASIGNADO);

    public PedidoService(PedidoRepositoryPort pedidoRepository,
                         TarifaDomicilioService tarifaDomicilioService) {
        this.pedidoRepository = pedidoRepository;
        this.tarifaDomicilioService = tarifaDomicilioService;
    }

    // Cliente crea pedido
    public PedidoDTO crearPedidoParaCliente(Long clienteId, CrearPedidoRequest req) {
        if (req.direccionRecogida == null || req.direccionRecogida.isBlank() ||
                req.barrioRecogida == null || req.barrioRecogida.isBlank() ||
                req.telefonoContactoRecogida == null || req.telefonoContactoRecogida.isBlank() ||
                req.direccionEntrega == null || req.direccionEntrega.isBlank() ||
                req.barrioEntrega == null || req.barrioEntrega.isBlank() ||
                req.nombreQuienRecibe == null || req.nombreQuienRecibe.isBlank() ||
                req.telefonoQuienRecibe == null || req.telefonoQuienRecibe.isBlank()) {
            throw new BadRequestException("Todos los datos de recogida y entrega son obligatorios");
        }

        var costo = tarifaDomicilioService.calcularCosto(
                req.barrioRecogida,
                req.barrioEntrega
        );

        Pedido p = new Pedido();
        p.setClienteId(clienteId);
        p.setEstado(EstadoPedido.CREADO);
        p.setFechaCreacion(LocalDateTime.now());

        p.setDireccionRecogida(req.direccionRecogida);
        p.setBarrioRecogida(req.barrioRecogida);
        p.setTelefonoContactoRecogida(req.telefonoContactoRecogida);

        p.setDireccionEntrega(req.direccionEntrega);
        p.setBarrioEntrega(req.barrioEntrega);
        p.setNombreQuienRecibe(req.nombreQuienRecibe);
        p.setTelefonoQuienRecibe(req.telefonoQuienRecibe);

        p.setCostoServicio(costo);

        Pedido guardado = pedidoRepository.save(p);
        return toDTO(guardado);
    }

    public List<PedidoDTO> listarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PedidoDTO> listarPorDomiciliario(Long domiciliarioId) {
        return pedidoRepository.findByDomiciliarioId(domiciliarioId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PedidoDTO> listarTodos() {
        return pedidoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    public PedidoDTO asignarDomiciliario(Long pedidoId, Long domiciliarioId) {
        if (domiciliarioId == null) {
            throw new BadRequestException("El domiciliarioId es obligatorio");
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));


        if (pedido.getEstado() != EstadoPedido.CREADO && pedido.getEstado() != EstadoPedido.ASIGNADO) {
            throw new BadRequestException("Solo se pueden asignar pedidos en estado CREADO o ASIGNADO");
        }

        pedido.setDomiciliarioId(domiciliarioId);
        pedido.setEstado(EstadoPedido.ASIGNADO);
        Pedido actualizado = pedidoRepository.save(pedido);
        return toDTO(actualizado);
    }


    public PedidoDTO actualizarEstado(Long pedidoId, String estadoStr, Long actorId, boolean esAdmin) {
        if (estadoStr == null || estadoStr.isBlank()) {
            throw new BadRequestException("El estado es obligatorio");
        }

        EstadoPedido nuevoEstado;
        try {
            nuevoEstado = EstadoPedido.valueOf(estadoStr);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Estado de pedido no válido: " + estadoStr);
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));


        if (!esAdmin) {
            if (pedido.getDomiciliarioId() == null ||
                    !pedido.getDomiciliarioId().equals(actorId)) {
                throw new ForbiddenException("No puedes modificar pedidos de otro domiciliario");
            }
        }


        validarTransicionEstado(pedido.getEstado(), nuevoEstado, esAdmin);

        pedido.setEstado(nuevoEstado);
        Pedido actualizado = pedidoRepository.save(pedido);
        return toDTO(actualizado);
    }

    private void validarTransicionEstado(EstadoPedido actual, EstadoPedido nuevo, boolean esAdmin) {
        if (esAdmin) {
            return;
        }

        if (nuevo == EstadoPedido.EN_CAMINO && !PERMITIR_EN_CAMINO_DESDE.contains(actual)) {
            throw new BadRequestException("Solo se puede pasar a EN_CAMINO desde ASIGNADO");
        }

        if (nuevo == EstadoPedido.ENTREGADO && !PERMITIR_ENTREGADO_DESDE.contains(actual)) {
            throw new BadRequestException("Solo se puede pasar a ENTREGADO desde EN_CAMINO");
        }

        if (nuevo == EstadoPedido.CANCELADO && !PERMITIR_CANCELADO_DESDE.contains(actual)) {
            throw new BadRequestException("No se puede cancelar un pedido en el estado actual: " + actual);
        }


    }

    public PedidoDTO toDTO(Pedido p) {
        PedidoDTO dto = new PedidoDTO();
        dto.id = p.getId();
        dto.clienteId = p.getClienteId();
        dto.domiciliarioId = p.getDomiciliarioId();
        dto.estado = p.getEstado() != null ? p.getEstado().name() : null;
        dto.costoServicio = p.getCostoServicio();
        dto.fechaCreacion = p.getFechaCreacion() != null ? p.getFechaCreacion().toString() : null;

        dto.direccionRecogida = p.getDireccionRecogida();
        dto.barrioRecogida = p.getBarrioRecogida();
        dto.telefonoContactoRecogida = p.getTelefonoContactoRecogida();

        dto.direccionEntrega = p.getDireccionEntrega();
        dto.barrioEntrega = p.getBarrioEntrega();
        dto.nombreQuienRecibe = p.getNombreQuienRecibe();
        dto.telefonoQuienRecibe = p.getTelefonoQuienRecibe();

        return dto;
    }

    public PedidoDTO cancelarPedidoPorCliente(Long pedidoId, Long clienteId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));


        if (!pedido.getClienteId().equals(clienteId)) {
            throw new ForbiddenException("No puedes cancelar pedidos de otros clientes");
        }


        if (!(pedido.getEstado() == EstadoPedido.CREADO ||
                pedido.getEstado() == EstadoPedido.ASIGNADO)) {
            throw new BadRequestException("Solo puedes cancelar pedidos en estado CREADO o ASIGNADO");
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        Pedido actualizado = pedidoRepository.save(pedido);

        return toDTO(actualizado);
    }
}
