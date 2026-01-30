package com.gofast.domicilios.application.service;

import com.gofast.domicilios.application.dto.DeliveryDTO;
import com.gofast.domicilios.domain.model.EstadoDelivery;
import com.gofast.domicilios.domain.model.Usuario;
import com.gofast.domicilios.domain.repository.UsuarioRepositoryPort;
import com.gofast.domicilios.infrastructure.persistence.entity.UsuarioEntity;
import com.gofast.domicilios.infrastructure.realtime.RealtimePublisher;
import com.gofast.domicilios.infrastructure.persistence.jpa.UsuarioJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
public class DeliveryService {
    private final UsuarioJpaRepository userRepo;
    private final RealtimePublisher realtime;

    public DeliveryService(UsuarioJpaRepository userRepo, RealtimePublisher realtime) {
        this.userRepo = userRepo;
        this.realtime = realtime;
    }

    @Transactional
    public void setDisponible(String email, boolean disponible) {
        UsuarioEntity u = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (u.getEstadoDelivery() == EstadoDelivery.POR_ENTREGAR) {
            throw new RuntimeException("No puedes cambiar disponibilidad con un pedido activo");
        }

        if (disponible) {
            u.setEstadoDelivery(EstadoDelivery.DISPONIBLE);
            u.setDisponibleDesde(LocalDateTime.now());
        } else {
            u.setEstadoDelivery(EstadoDelivery.DESCONECTADO);
            u.setDisponibleDesde(null);
        }

        userRepo.save(u);
        realtime.deliveryActualizado(toDto(u));
    }


    public DeliveryDTO toDto(UsuarioEntity u) {
        return new DeliveryDTO(
                u.getId(),
                u.getEmail(),
                u.getEstadoDelivery().name(),
                u.getDisponibleDesde()
        );
    }
}
