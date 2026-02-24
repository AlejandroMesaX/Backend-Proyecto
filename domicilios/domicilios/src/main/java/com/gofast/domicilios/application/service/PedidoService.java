package com.gofast.domicilios.application.service;

import com.gofast.domicilios.application.dto.*;
import com.gofast.domicilios.application.exception.BadRequestException;
import com.gofast.domicilios.application.exception.ForbiddenException;
import com.gofast.domicilios.application.exception.NotFoundException;
import com.gofast.domicilios.domain.model.*;
import com.gofast.domicilios.domain.repository.BarrioRepositoryPort;
import com.gofast.domicilios.domain.repository.DireccionRepositoryPort;
import com.gofast.domicilios.domain.repository.PedidoRepositoryPort;
import com.gofast.domicilios.domain.repository.UsuarioRepositoryPort;
import com.gofast.domicilios.domain.service.TarifaDomicilioService;
import com.gofast.domicilios.infrastructure.realtime.PedidoRealtimePublisher;
import com.gofast.domicilios.infrastructure.realtime.RealtimePublisher;
import com.gofast.domicilios.infrastructure.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PedidoService {

    private final PedidoRepositoryPort pedidoRepository;
    private final TarifaDomicilioService tarifaDomicilioService;
    private final UsuarioRepositoryPort usuarioRepository;
    private final DireccionRepositoryPort direccionRepository;
    private final BarrioRepositoryPort barrioRepository;
    private final PedidoRealtimePublisher pedidoRealtimePublisher;
    private final RealtimePublisher realtimePublisher;
    private final DeliveryService deliveryService;
    private static final Set<EstadoPedido> PERMITIR_CANCELADO_DESDE =
            EnumSet.of(EstadoPedido.CREADO, EstadoPedido.ASIGNADO);

    public PedidoService(PedidoRepositoryPort pedidoRepository,
                         TarifaDomicilioService tarifaDomicilioService,
                         UsuarioRepositoryPort usuarioRepository,
                         DireccionRepositoryPort direccionRepository,
                         BarrioRepositoryPort barrioRepository,
                         PedidoRealtimePublisher pedidoRealtimePublisher,
                         RealtimePublisher realtimePublisher,
                         DeliveryService deliveryService) {
        this.pedidoRepository = pedidoRepository;
        this.tarifaDomicilioService = tarifaDomicilioService;
        this.usuarioRepository = usuarioRepository;
        this.direccionRepository = direccionRepository;
        this.barrioRepository = barrioRepository;
        this.pedidoRealtimePublisher = pedidoRealtimePublisher;
        this.realtimePublisher = realtimePublisher;
        this.deliveryService = deliveryService;
    }

    @Transactional
    public PedidoDTO crearPedidoParaCliente(Long clienteId, CrearPedidoRequest req) {
        if (req.direccionId != null) {
            Direccion dir = direccionRepository.findById(req.direccionId)
                    .orElseThrow(() -> {
                        log.warn(
                                "Dirección no encontrada al crear pedido. direccionId='{}'",
                                req.direccionId);
                        return new NotFoundException(
                                "Dirección no encontrada",
                                "DIRECCION_NOT_FOUND");
                    });

            if (!Objects.equals(dir.getClienteId(), clienteId)) {
                log.warn(
                        "Cliente '{}' intentó usar dirección '{}' que no le pertenece",
                        clienteId, req.direccionId);
                throw new ForbiddenException(
                        "No tienes permiso para usar esta dirección",
                        "DIRECCION_NO_PERMITIDA");
            }

            if (!dir.getActivo()) {
                throw new BadRequestException(
                        "La dirección seleccionada está inactiva",
                        "DIRECCION_INACTIVA",
                        "direccion");
            }

            Barrio barrio = barrioRepository.findById(dir.getBarrioId())
                    .orElseThrow(() -> {
                        log.warn(
                                "Barrio no encontrado para dirección. barrioId='{}'",
                                dir.getBarrioId());
                        return new NotFoundException(
                                "Barrio no encontrado",
                                "BARRIO_NOT_FOUND");
                    });

            if (!barrio.isActivo()) {
                throw new BadRequestException(
                        "El barrio de la dirección está inactivo",
                        "BARRIO_INACTIVO",
                        "barrio");
            }
            
            if (req.direccionId == null) {
                if (req.direccionRecogida == null || req.direccionRecogida.isBlank() ||
                        req.barrioRecogida == null || req.barrioRecogida.isBlank() ||
                        req.telefonoContactoRecogida == null || req.telefonoContactoRecogida.isBlank()) {
                    throw new BadRequestException(
                            "Los datos de recogida son obligatorios cuando no se usa una dirección guardada",
                            "DATOS_RECOGIDA_INCOMPLETOS");
                }
            }

            req.direccionRecogida = dir.getDireccionRecogida();
            req.telefonoContactoRecogida = dir.getTelefonoContacto();
            req.barrioRecogida = barrio.getNombre();
        }

        var costo = tarifaDomicilioService.calcularCosto(req.barrioRecogida, req.barrioEntrega);

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
        pedidoRealtimePublisher.pedidoCreado(toDTO(guardado));

        return toDTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO> listarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO> listarPorDomiciliario(Long domiciliarioId) {
        return pedidoRepository.findByDomiciliarioId(domiciliarioId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO> listarPedidos(Long clienteId, Long domiciliarioId) {
        return pedidoRepository.findByFiltros(clienteId, domiciliarioId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO> misPedidosEntregadosComoDomiciliario(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ForbiddenException(
                    "No autenticado",
                    "NO_AUTENTICADO");
        }

        Usuario u = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ForbiddenException(
                        "Usuario no encontrado",
                        "USUARIO_NOT_FOUND"));

        if (u.getRol() != Rol.DELIVERY) {
            throw new ForbiddenException(
                    "Solo domiciliarios pueden realizar esta acción",
                    "ROL_NO_PERMITIDO");
        }
        if (!u.isActivo()) {
            throw new ForbiddenException(
                    "Usuario inactivo",
                    "USUARIO_INACTIVO");
        }

        return pedidoRepository.findEntregadosByDomiciliarioId(u.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public PedidoDTO asignarPedido(Long pedidoId, AsignarDomiciliarioRequest req) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> {
                    log.warn(
                            "Pedido no encontrado al asignar. pedidoId='{}'",
                            pedidoId);
                    return new NotFoundException(
                            "Pedido no encontrado",
                            "PEDIDO_NOT_FOUND");
                });

        Usuario domi = usuarioRepository.findById(req.domiciliarioId())
                .orElseThrow(() -> {
                    log.warn(
                            "Domiciliario no encontrado al asignar. domiciliarioId='{}'",
                            req.domiciliarioId());
                    return new NotFoundException(
                            "Domiciliario no encontrado",
                            "DOMICILIARIO_NOT_FOUND");
                });

        if (domi.getRol() != Rol.DELIVERY) {
            throw new BadRequestException(
                    "El usuario no tiene rol de domiciliario",
                    "ROL_INVALIDO",
                    "domiciliario");
        }

        if (!domi.isActivo()) {
            throw new BadRequestException(
                    "No se puede asignar a un domiciliario inactivo",
                    "DOMICILIARIO_INACTIVO",
                    "domiciliario");
        }

        if (domi.getEstadoDelivery() != EstadoDelivery.DISPONIBLE) {
            throw new BadRequestException(
                    "El domiciliario no está disponible",
                    "DOMICILIARIO_NO_DISPONIBLE",
                    "domiciliario");
        }

        if (pedido.getEstado() != EstadoPedido.CREADO
                && pedido.getEstado() != EstadoPedido.INCIDENCIA) {
            throw new BadRequestException(
                    "Solo se puede asignar un pedido en estado CREADO o INCIDENCIA",
                    "ESTADO_PEDIDO_INVALIDO",
                    "pedido");
        }

        pedido.setDomiciliarioId(req.domiciliarioId());
        pedido.setEstado(EstadoPedido.ASIGNADO);
        Pedido saved = pedidoRepository.save(pedido);

        domi.setEstadoDelivery(EstadoDelivery.POR_RECOGER);
        domi.setDisponibleDesde(null);
        usuarioRepository.save(domi);

        PedidoDTO dto = toDTO(saved);
        realtimePublisher.pedidoActualizado(dto);
        realtimePublisher.deliveryActualizado(deliveryService.toDto(domi));
        realtimePublisher.pedidoParaDelivery(domi.getId(), dto);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO> listarPedidosDelDomiciliario(Authentication authentication,
                                                        EstadoPedido estado) {
        if (authentication == null || authentication.getName() == null) {
            throw new ForbiddenException(
                    "No autenticado",
                    "NO_AUTENTICADO");
        }

        Usuario u = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ForbiddenException(
                        "Usuario no encontrado",
                        "USUARIO_NOT_FOUND"));

        if (u.getRol() != Rol.DELIVERY) {
            throw new ForbiddenException(
                    "Solo domiciliarios pueden ver esta ruta",
                    "ROL_NO_PERMITIDO");
        }
        if (!u.isActivo()) {
            throw new ForbiddenException(
                    "Usuario inactivo",
                    "USUARIO_INACTIVO");
        }

        return pedidoRepository.findByDomiciliarioIdYEstado(u.getId(), estado)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public PedidoDTO cambiarEstadoComoDomiciliario(Authentication authentication, Long pedidoId) {
        if (authentication == null || authentication.getName() == null) {
            throw new ForbiddenException(
                    "No autenticado",
                    "NO_AUTENTICADO");
        }

        Usuario u = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ForbiddenException(
                        "Usuario no encontrado",
                        "USUARIO_NOT_FOUND"));

        if (u.getRol() != Rol.DELIVERY) {
            throw new ForbiddenException(
                    "Solo domiciliarios pueden cambiar estado",
                    "ROL_NO_PERMITIDO");
        }
        if (!u.isActivo()) {
            throw new ForbiddenException(
                    "Usuario inactivo",
                    "USUARIO_INACTIVO");
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> {
                    log.warn(
                            "Pedido no encontrado al cambiar estado. pedidoId='{}'",
                            pedidoId);
                    return new NotFoundException(
                            "Pedido no encontrado",
                            "PEDIDO_NOT_FOUND");
                });

        if (!Objects.equals(pedido.getDomiciliarioId(), u.getId())) {
            log.warn(
                    "Domiciliario '{}' intentó cambiar estado de pedido '{}' que no le pertenece",
                    u.getId(), pedidoId);
            throw new ForbiddenException(
                    "No tienes permiso para modificar este pedido",
                    "PEDIDO_NO_ASIGNADO");
        }

        if (pedido.getEstado() == EstadoPedido.ASIGNADO
                && u.getEstadoDelivery() == EstadoDelivery.POR_RECOGER) {
            pedido.setEstado(EstadoPedido.EN_CAMINO);
            u.setEstadoDelivery(EstadoDelivery.POR_ENTREGAR);
        } else if (pedido.getEstado() == EstadoPedido.EN_CAMINO
                && u.getEstadoDelivery() == EstadoDelivery.POR_ENTREGAR) {
            pedido.setEstado(EstadoPedido.ENTREGADO);
            u.setEstadoDelivery(EstadoDelivery.DISPONIBLE);
            u.setDisponibleDesde(LocalDateTime.now());
        } else {
            throw new BadRequestException(
                    "La transición de estado no es válida en el estado actual",
                    "TRANSICION_INVALIDA",
                    "estado");
        }

        Pedido saved = pedidoRepository.save(pedido);
        usuarioRepository.save(u);

        PedidoDTO dto = toDTO(saved);
        realtimePublisher.pedidoActualizado(dto);
        realtimePublisher.deliveryActualizado(deliveryService.toDto(u));

        return dto;
    }

    @Transactional
    public void cancelarPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> {
                    log.warn(
                            "Pedido no encontrado al cancelar. pedidoId='{}'",
                            pedidoId);
                    return new NotFoundException(
                            "Pedido no encontrado",
                            "PEDIDO_NOT_FOUND");
                });

        if (!PERMITIR_CANCELADO_DESDE.contains(pedido.getEstado())) {
            throw new BadRequestException(
                    "No se puede cancelar un pedido en el estado actual",
                    "ESTADO_PEDIDO_INVALIDO",
                    "estado");
        }

        pedido.setEstado(EstadoPedido.CANCELADO);

        if (pedido.getDomiciliarioId() != null) {
            Usuario u = usuarioRepository.findById(pedido.getDomiciliarioId())
                    .orElseThrow(() -> {
                        log.warn(
                                "Domiciliario no encontrado al cancelar pedido. id='{}'",
                                pedido.getDomiciliarioId());
                        return new NotFoundException(
                                "Domiciliario no encontrado",
                                "DOMICILIARIO_NOT_FOUND");
                    });

            u.setEstadoDelivery(EstadoDelivery.DISPONIBLE);
            u.setDisponibleDesde(LocalDateTime.now());
            usuarioRepository.save(u);

            realtimePublisher.pedidoParaDelivery(pedido.getDomiciliarioId(), toDTO(pedido));
            realtimePublisher.deliveryActualizado(deliveryService.toDto(u));
        }

        Pedido saved = pedidoRepository.save(pedido);
        realtimePublisher.pedidoActualizado(toDTO(saved));
    }

    @Transactional
    public PedidoDTO reportarIncidenciaComoDomiciliario(Authentication authentication,
                                                        Long pedidoId,
                                                        ReportarIncidenciaRequest req) {
        if (authentication == null || authentication.getName() == null) {
            throw new ForbiddenException(
                    "No autenticado",
                    "NO_AUTENTICADO");
        }

        Usuario u = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ForbiddenException(
                        "Usuario no encontrado",
                        "USUARIO_NOT_FOUND"));

        if (u.getRol() != Rol.DELIVERY) {
            throw new ForbiddenException(
                    "Solo domiciliarios pueden reportar incidencias",
                    "ROL_NO_PERMITIDO");
        }
        if (!u.isActivo()) {
            throw new ForbiddenException(
                    "Usuario inactivo",
                    "USUARIO_INACTIVO");
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> {
                    log.warn(
                            "Pedido no encontrado al reportar incidencia. pedidoId='{}'",
                            pedidoId);
                    return new NotFoundException(
                            "Pedido no encontrado",
                            "PEDIDO_NOT_FOUND");
                });

        if (!Objects.equals(pedido.getDomiciliarioId(), u.getId())) {
            log.warn(
                    "Domiciliario '{}' intentó reportar incidencia de pedido '{}' que no le pertenece",
                    u.getId(), pedidoId);
            throw new ForbiddenException(
                    "No puedes reportar incidencia de un pedido que no es tuyo",
                    "PEDIDO_NO_ASIGNADO");
        }

        if (pedido.getEstado() != EstadoPedido.ASIGNADO
                && pedido.getEstado() != EstadoPedido.EN_CAMINO) {
            throw new BadRequestException(
                    "Solo puedes reportar incidencia si el pedido está ASIGNADO o EN_CAMINO",
                    "ESTADO_PEDIDO_INVALIDO",
                    "incidencia");
        }

        pedido.setEstado(EstadoPedido.INCIDENCIA);
        pedido.setMotivoIncidencia(req.motivo().trim());
        pedido.setFechaIncidencia(LocalDateTime.now());

        Pedido saved = pedidoRepository.save(pedido);

        u.setEstadoDelivery(EstadoDelivery.DISPONIBLE);
        u.setDisponibleDesde(LocalDateTime.now());
        usuarioRepository.save(u);

        PedidoDTO dto = toDTO(saved);
        realtimePublisher.pedidoActualizado(dto);
        realtimePublisher.deliveryActualizado(deliveryService.toDto(u));
        realtimePublisher.pedidoParaDelivery(u.getId(), dto);

        return dto;
    }

    @Transactional
    public PedidoDTO cancelarPedidoPorCliente(Long pedidoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException(
                    "Usuario no autenticado",
                    "NO_AUTENTICADO");
        }

        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new ForbiddenException(
                    "Principal inválido",
                    "PRINCIPAL_INVALIDO");
        }

        Long clienteId = userDetails.getId();

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> {
                    log.warn(
                            "Pedido no encontrado al cancelar por cliente. pedidoId='{}'",
                            pedidoId);
                    return new NotFoundException(
                            "Pedido no encontrado",
                            "PEDIDO_NOT_FOUND");
                });

        if (!pedido.getClienteId().equals(clienteId)) {
            log.warn(
                    "Cliente '{}' intentó cancelar pedido '{}' de otro cliente",
                    clienteId, pedidoId);
            throw new ForbiddenException(
                    "No puedes cancelar pedidos de otro cliente",
                    "PEDIDO_NO_PERMITIDO");
        }

        if (!PERMITIR_CANCELADO_DESDE.contains(pedido.getEstado())) {
            throw new BadRequestException(
                    "Solo puedes cancelar pedidos en estado CREADO o ASIGNADO",
                    "ESTADO_PEDIDO_INVALIDO",
                    "pedido");
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        Pedido saved = pedidoRepository.save(pedido);

        realtimePublisher.pedidoActualizado(toDTO(saved));

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO> listarPedidosDelCliente(LocalDate desde, LocalDate hasta) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException(
                    "Usuario no autenticado",
                    "NO_AUTENTICADO");
        }

        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new ForbiddenException(
                    "Principal inválido",
                    "PRINCIPAL_INVALIDO");
        }

        Long clienteId = userDetails.getId();

        return pedidoRepository.findByClienteYFecha(clienteId, desde, hasta)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private PedidoDTO toDTO(Pedido p) {
        PedidoDTO dto = new PedidoDTO();
        dto.id = p.getId();
        dto.clienteId = p.getClienteId();
        dto.domiciliarioId = p.getDomiciliarioId();
        dto.estado = p.getEstado().name();
        dto.costoServicio = p.getCostoServicio();
        dto.fechaCreacion = p.getFechaCreacion() != null
                ? p.getFechaCreacion().toString() : null;
        dto.direccionRecogida = p.getDireccionRecogida();
        dto.barrioRecogida = p.getBarrioRecogida();
        dto.telefonoContactoRecogida = p.getTelefonoContactoRecogida();
        dto.direccionEntrega = p.getDireccionEntrega();
        dto.barrioEntrega = p.getBarrioEntrega();
        dto.nombreQuienRecibe = p.getNombreQuienRecibe();
        dto.telefonoQuienRecibe = p.getTelefonoQuienRecibe();
        dto.motivoIncidencia = p.getMotivoIncidencia();
        dto.fechaIncidencia = p.getFechaIncidencia() != null
                ? p.getFechaIncidencia().toString() : null;
        return dto;
    }
}
