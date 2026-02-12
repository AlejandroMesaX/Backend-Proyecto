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
import com.gofast.domicilios.infrastructure.persistence.entity.PedidoEntity;
import com.gofast.domicilios.infrastructure.persistence.entity.UsuarioEntity;
import com.gofast.domicilios.infrastructure.persistence.jpa.PedidoJpaRepository;
import com.gofast.domicilios.infrastructure.persistence.jpa.UsuarioJpaRepository;
import com.gofast.domicilios.infrastructure.realtime.PedidoRealtimePublisher;
import com.gofast.domicilios.infrastructure.realtime.RealtimePublisher;
import com.gofast.domicilios.infrastructure.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
public class PedidoService {

    private final PedidoRepositoryPort pedidoRepository;
    private final TarifaDomicilioService tarifaDomicilioService;
    private final UsuarioJpaRepository usuarioRepository;
    private final DireccionRepositoryPort direccionRepository;
    private final BarrioRepositoryPort barrioRepository;
    private final PedidoRealtimePublisher pedidoRealtimePublisher;
    private final RealtimePublisher realtimePublisher;
    private final DeliveryService deliveryService;
    private final DeliveryPedidosService pedidosService;
    private final SimpMessagingTemplate messagingTemplate;


    private static final Set<EstadoPedido> PERMITIR_EN_CAMINO_DESDE =
            EnumSet.of(EstadoPedido.ASIGNADO);
    private static final Set<EstadoPedido> PERMITIR_ENTREGADO_DESDE =
            EnumSet.of(EstadoPedido.EN_CAMINO);
    private static final Set<EstadoPedido> PERMITIR_CANCELADO_DESDE =
            EnumSet.of(EstadoPedido.CREADO, EstadoPedido.ASIGNADO);

    public PedidoService(PedidoRepositoryPort pedidoRepository,
                         TarifaDomicilioService tarifaDomicilioService,
                         UsuarioJpaRepository usuarioRepository,
                         DireccionRepositoryPort direccionRepository,
                         BarrioRepositoryPort barrioRepository,
                         PedidoRealtimePublisher pedidoRealtimePublisher,
                         RealtimePublisher realtimePublisher,
                         DeliveryService deliveryService,
                         SimpMessagingTemplate messagingTemplate,
                         DeliveryPedidosService pedidosService) {
        this.pedidoRepository = pedidoRepository;
        this.tarifaDomicilioService = tarifaDomicilioService;
        this.usuarioRepository = usuarioRepository;
        this.direccionRepository = direccionRepository;
        this.barrioRepository = barrioRepository;
        this.pedidoRealtimePublisher = pedidoRealtimePublisher;
        this.realtimePublisher = realtimePublisher;
        this.deliveryService = deliveryService;
        this.messagingTemplate = messagingTemplate;
        this.pedidosService = pedidosService;
    }

    // Cliente crea pedido
    public PedidoDTO crearPedidoParaCliente(Long clienteId, CrearPedidoRequest req) {
        if (req.direccionId != null) {

            Direccion dir = direccionRepository.findById(req.direccionId)
                    .orElseThrow(() -> new BadRequestException("La dirección seleccionada no existe"));

            // Debe pertenecer al cliente
            if (!Objects.equals(dir.getClienteId(), clienteId)) {
                throw new ForbiddenException("Esa dirección no te pertenece");
            }

            // Debe estar activa
            if (!Boolean.TRUE.equals(dir.getActivo())) {
                throw new BadRequestException("La dirección seleccionada está inactiva");
            }

            // Obtener nombre del barrio (porque tu pedido usa barrioRecogida como String)
            var barrio = barrioRepository.findById(dir.getBarrioId())
                    .orElseThrow(() -> new BadRequestException("El barrio de la dirección no existe"));

            if (!Boolean.TRUE.equals(barrio.isActivo())) {
                throw new BadRequestException("El barrio de la dirección está inactivo");
            }

            // ✅ Snapshot en el request (si el front no mandó estos campos, los llenamos)
            req.direccionRecogida = dir.getDireccionRecogida();
            req.telefonoContactoRecogida = dir.getTelefonoContacto();
            req.barrioRecogida = barrio.getNombre();
        }

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
        pedidoRealtimePublisher.pedidoCreado(toDTO(guardado));

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

    public List<PedidoDTO> listarPedidos(Long clienteId, Long domiciliarioId) {
        return pedidoRepository.findByFiltros(clienteId, domiciliarioId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PedidoDTO> misPedidosEntregadosComoDomiciliario(Authentication authentication) {
        UsuarioEntity u = usuarioDesdeAuth(authentication);

        if (u.getRol() != Rol.DELIVERY) throw new ForbiddenException("Solo domiciliarios");
        if (!u.isActivo()) throw new ForbiddenException("Usuario inactivo");

        return pedidoRepository.findEntregadosByDomiciliarioId(u.getId())
                .stream()
                .map(this::toDTO) // tu mapper domain->DTO
                .toList();
    }



    public PedidoDTO asignarPedido(Long pedidoId, AsignarDomiciliarioRequest req) {

        if (req == null || req.domiciliarioId == null) {
            throw new BadRequestException("domiciliarioId es obligatorio");
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));

        // Validar domiciliario
        UsuarioEntity domi = usuarioRepository.findById(req.domiciliarioId)
                .orElseThrow(() -> new NotFoundException("Domiciliario no encontrado"));

        // ✅ rol correcto
        if (domi.getRol() != Rol.DELIVERY) { // o el nombre que uses para domiciliario
            throw new BadRequestException("El usuario no tiene rol DOMICILIARIO/DELIVERY");
        }

        if (domi.getEstadoDelivery() != EstadoDelivery.DISPONIBLE) {
            throw new RuntimeException("El domiciliario ya no está disponible");
        }

        // ✅ activo (si manejas activo en usuario)
        if (!domi.isActivo()) {
            throw new BadRequestException("No se puede asignar a un domiciliario inactivo");
        }

        // (Opcional) regla de negocio: solo asignar si está pendiente
        // Si tu estado es String:
        if (pedido.getEstado() != EstadoPedido.CREADO && pedido.getEstado() != EstadoPedido.INCIDENCIA) {
            throw new BadRequestException(
                    "Solo se puede asignar un pedido en estado CREADO o INCIDENCIA"
            );
        }

        pedido.setDomiciliarioId(req.domiciliarioId);
        pedido.setEstado(EstadoPedido.ASIGNADO);
        Pedido saved = pedidoRepository.save(pedido);

        domi.setEstadoDelivery(EstadoDelivery.POR_RECOGER);
        domi.setDisponibleDesde(null);
        usuarioRepository.save(domi);

        PedidoDTO dto = toDTO(pedido);

        realtimePublisher.pedidoActualizado(dto);
        realtimePublisher.deliveryActualizado(deliveryService.toDto(domi));
        realtimePublisher.pedidoParaDelivery(domi.getId(), dto);

        return toDTO(saved); // tu mapper exacto (el que ya tienes)
    }

    private UsuarioEntity usuarioDesdeAuth(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ForbiddenException("No autenticado");
        }
        String email = authentication.getName(); // normalmente es el username/email
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Usuario no encontrado en el token"));
    }

    public List<PedidoDTO> listarPedidosDelDomiciliario(Authentication authentication, EstadoPedido estado) {

        UsuarioEntity u = usuarioDesdeAuth(authentication);

        if (u.getRol() != Rol.DELIVERY) {
            throw new ForbiddenException("Solo domiciliarios pueden ver esta ruta");
        }
        if (!u.isActivo()) {
            throw new ForbiddenException("Usuario inactivo");
        }

        List<Pedido> pedidos = pedidoRepository.findByDomiciliarioIdYEstado(u.getId(), estado);

        return pedidos.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public PedidoDTO cambiarEstadoComoDomiciliario(
            Authentication authentication,
            Long pedidoId,
            ActualizarEstadoPedidoRequest req
    ) {
        if (req == null || req.estado == null || req.estado.isBlank()) {
            throw new BadRequestException("estado es obligatorio");
        }

        UsuarioEntity u = usuarioDesdeAuth(authentication);

        if (u.getRol() != Rol.DELIVERY) {
            throw new ForbiddenException("Solo domiciliarios pueden cambiar estado");
        }
        if (!u.isActivo()) {
            throw new ForbiddenException("Usuario inactivo");
        }

//        EstadoPedido nuevoEstado;
//        try {
//            nuevoEstado = EstadoPedido.valueOf(req.estado.trim().toUpperCase());
//        } catch (Exception e) {
//            throw new BadRequestException("Estado inválido: " + req.estado);
//        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));


        if(pedido.getEstado() == EstadoPedido.ASIGNADO && u.getEstadoDelivery() == EstadoDelivery.POR_RECOGER) {
            pedido.setEstado(EstadoPedido.EN_CAMINO);
            u.setEstadoDelivery(EstadoDelivery.POR_ENTREGAR);
        }else if (pedido.getEstado() == EstadoPedido.EN_CAMINO && u.getEstadoDelivery() == EstadoDelivery.POR_ENTREGAR) {
                pedido.setEstado(EstadoPedido.ENTREGADO);
                u.setEstadoDelivery(EstadoDelivery.DISPONIBLE);
                u.setDisponibleDesde(LocalDateTime.now());
        }

        Pedido saved = pedidoRepository.save(pedido);
        UsuarioEntity save = usuarioRepository.save(u);

        realtimePublisher.pedidoActualizado(toDTO(saved));
        realtimePublisher.deliveryActualizado(toDto(save));
        return toDTO(saved);
    }

    @Transactional
    public void cancelarPedido(Long pedidoId) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));

        //Long domId = pedido.getDomiciliarioId();

        pedido.setEstado(EstadoPedido.CANCELADO);

        if(pedido.getDomiciliarioId() != null){
            UsuarioEntity u = usuarioRepository.findById(pedido.getDomiciliarioId())
                    .orElseThrow(() -> new NotFoundException("Domiciliario no encontrado"));
            u.setEstadoDelivery(EstadoDelivery.DISPONIBLE);
            u.setDisponibleDesde(LocalDateTime.now());
            UsuarioEntity save = usuarioRepository.save(u);
            realtimePublisher.pedidoParaDelivery(pedido.getDomiciliarioId(), toDTO(pedido));
            realtimePublisher.deliveryActualizado(toDto(save));
        }

        Pedido saved = pedidoRepository.save(pedido);

        realtimePublisher.pedidoActualizado(toDTO(saved));

//        // ✅ marcar como cancelado (recomendado: NO borrar físico)
//        pedido.setEstado(EstadoPedido.CANCELADO);
//        Pedido saved = pedidoRepository.save(pedido);
//
//        // ✅ notificar admin pedidos
//        realtimePublisher.pedidoActualizado(toDTO(saved));
//
//        // ✅ si había delivery asignado, avisarle para que lo quite del panel
//        if (domId != null) {
//            realtimePublisher.pedidoParaDelivery(domId, toDTO(saved));
//
//            // ✅ devolver el delivery a disponible
//            UsuarioEntity dom = usuarioRepository.findById(domId)
//                    .orElseThrow(() -> new NotFoundException("Domiciliario no encontrado"));
//
//            dom.setEstadoDelivery(EstadoDelivery.DISPONIBLE);
//            dom.setDisponibleDesde(LocalDateTime.now());
//            UsuarioEntity save = usuarioRepository.save(dom);
//
//            realtimePublisher.deliveryActualizado(toDto(save));
//        }
    }

    @Transactional
    public PedidoDTO reportarIncidenciaComoDomiciliario(
            Authentication authentication,
            Long pedidoId,
            ReportarIncidenciaRequest req
    ) {
        System.out.println("MOTIVO RECIBIDO: " + (req != null ? req.motivo : "null"));

        if (req == null || req.motivo == null || req.motivo.isBlank()) {
            throw new BadRequestException("motivo es obligatorio");
        }

        UsuarioEntity u = usuarioDesdeAuth(authentication);

        if (u.getRol() != Rol.DELIVERY) throw new ForbiddenException("Solo domiciliarios");
        if (!u.isActivo()) throw new ForbiddenException("Usuario inactivo");

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));

        // Solo si está asignado a mí
        if (pedido.getDomiciliarioId() == null || !pedido.getDomiciliarioId().equals(u.getId())) {
            throw new ForbiddenException("No puedes reportar incidencia de un pedido que no es tuyo");
        }

        // Solo si está en un estado “activo”
        if (pedido.getEstado() != EstadoPedido.ASIGNADO && pedido.getEstado() != EstadoPedido.EN_CAMINO) {
            throw new BadRequestException("Solo puedes pedir ayuda si el pedido está ASIGNADO o EN_CAMINO");
        }

        // 1) Pedido -> INCIDENCIA + motivo
        pedido.setEstado(EstadoPedido.INCIDENCIA);
        pedido.setMotivoIncidencia(req.motivo.trim());
        pedido.setFechaIncidencia(LocalDateTime.now());

        System.out.println("ANTES SAVE motivo: " + pedido.getMotivoIncidencia());
        Pedido saved = pedidoRepository.save(pedido);
        System.out.println("DESPUES SAVE motivo: " + saved.getMotivoIncidencia());

        PedidoDTO pedidoDto = toDTO(saved);
        System.out.println("MOTIVO GUARDADO: " + saved.getMotivoIncidencia());

        // 2) Delivery vuelve a DISPONIBLE
        u.setEstadoDelivery(EstadoDelivery.DISPONIBLE);
        u.setDisponibleDesde(LocalDateTime.now());
        UsuarioEntity savedU = usuarioRepository.save(u);

        // 3) Realtime:
        // admin ve pedido con INCIDENCIA
        realtimePublisher.pedidoActualizado(pedidoDto);

        // admin actualiza FIFO (domiciliario DISPONIBLE)
        realtimePublisher.deliveryActualizado(toDto(savedU));

        // delivery recibe pedido INCIDENCIA (tu hook lo puede ocultar o mostrar “en incidencia”)
        realtimePublisher.pedidoParaDelivery(u.getId(), pedidoDto);

        return pedidoDto;
    }


    public void cancelarPedidoPorCliente(Long pedidoId) {

        Long clienteIdLogueado = getUsuarioLogueadoId();

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));

        if (!pedido.getClienteId().equals(clienteIdLogueado)) {
            throw new ForbiddenException("No puedes cancelar pedidos de otro cliente");
        }

        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new BadRequestException("No se puede cancelar un pedido entregado");
        }

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            return;
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    public List<PedidoDTO> listarPedidosDelCliente(LocalDate desde, LocalDate hasta) {

        Long clienteId = getUsuarioLogueadoId(); // el helper que ya hicimos con SecurityContextHolder

        return pedidoRepository.findByClienteYFecha(clienteId, desde, hasta)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private Long getUsuarioLogueadoId() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new RuntimeException("Principal inválido");
        }

        return userDetails.getId();
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

    private PedidoDTO toDTO(Pedido p) {

        PedidoDTO dto = new PedidoDTO();

        dto.id = p.getId();
        dto.clienteId = p.getClienteId();
        dto.domiciliarioId = p.getDomiciliarioId();

        dto.estado = p.getEstado().name(); // si es enum
        dto.costoServicio = p.getCostoServicio();
        dto.fechaCreacion = p.getFechaCreacion() != null
                ? p.getFechaCreacion().toString()
                : null;

        dto.direccionRecogida = p.getDireccionRecogida();
        dto.barrioRecogida = p.getBarrioRecogida();
        dto.telefonoContactoRecogida = p.getTelefonoContactoRecogida();

        dto.direccionEntrega = p.getDireccionEntrega();
        dto.barrioEntrega = p.getBarrioEntrega();
        dto.nombreQuienRecibe = p.getNombreQuienRecibe();
        dto.telefonoQuienRecibe = p.getTelefonoQuienRecibe();

        dto.motivoIncidencia = p.getMotivoIncidencia();
        dto.fechaIncidencia = p.getFechaIncidencia() != null
                ? p.getFechaIncidencia().toString()
                : null;

        return dto;
    }

    public DeliveryDTO toDto(UsuarioEntity u) {
        return new DeliveryDTO(
                u.getId(),
                u.getEmail(),
                u.getEstadoDelivery().name(),
                u.getDisponibleDesde()
        );
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
