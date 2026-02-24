package com.gofast.domicilios.application.service;

import com.gofast.domicilios.application.dto.DeliveryDTO;
import com.gofast.domicilios.application.exception.BadRequestException;
import com.gofast.domicilios.application.exception.ForbiddenException;
import com.gofast.domicilios.application.exception.NotFoundException;
import com.gofast.domicilios.domain.model.EstadoDelivery;
import com.gofast.domicilios.domain.model.Rol;
import com.gofast.domicilios.domain.model.Usuario;
import com.gofast.domicilios.domain.repository.UsuarioRepositoryPort;
import com.gofast.domicilios.infrastructure.realtime.RealtimePublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;


@Service
public class DeliveryService {
    private final UsuarioRepositoryPort usuarioRepository;
    private final RealtimePublisher realtime;
    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    public DeliveryService(UsuarioRepositoryPort usuarioRepository, RealtimePublisher realtime) {
        this.usuarioRepository = usuarioRepository;
        this.realtime = realtime;
    }

    @Transactional
    public void setDisponible(String email, boolean disponible) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn(
                            "Usuario no encontrado al cambiar disponibilidad. email='{}'",
                            email);
                    return new NotFoundException(
                            "Usuario no encontrado",
                            "USUARIO_NOT_FOUND");
                });

        if (u.getRol() != Rol.DELIVERY) {
            log.warn(
                    "Usuario '{}' con rol '{}' intentó cambiar estado de delivery",
                    u.getId(), u.getRol());
            throw new ForbiddenException(
                    "Solo los domiciliarios pueden cambiar su disponibilidad",
                    "ROL_NO_PERMITIDO");
        }

        if (u.getEstadoDelivery() == EstadoDelivery.POR_ENTREGAR) {
            throw new BadRequestException(
                    "No puedes cambiar disponibilidad con un pedido activo",
                    "DELIVERY_CON_PEDIDO_ACTIVO",
                    "delivery");
        }

        if (disponible) {
            u.setEstadoDelivery(EstadoDelivery.DISPONIBLE);
            u.setDisponibleDesde(LocalDateTime.now());
        } else {
            u.setEstadoDelivery(EstadoDelivery.DESCONECTADO);
            u.setDisponibleDesde(null);
        }

        usuarioRepository.save(u);
        realtime.deliveryActualizado(toDto(u));
    }


    public DeliveryDTO toDto(Usuario u) {
        return new DeliveryDTO(
                u.getId(),
                u.getEmail(),
                u.getEstadoDelivery().name(),
                u.getDisponibleDesde()
        );
    }
}
