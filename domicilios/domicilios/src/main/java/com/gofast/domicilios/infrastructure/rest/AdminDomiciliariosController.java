package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.dto.DeliveryDTO;
import com.gofast.domicilios.application.service.DeliveryService;
import com.gofast.domicilios.infrastructure.persistence.jpa.UsuarioJpaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/domiciliarios")
public class AdminDomiciliariosController {
    private final UsuarioJpaRepository userRepo;
    private final DeliveryService deliveryService;

    public AdminDomiciliariosController(UsuarioJpaRepository userRepo, DeliveryService deliveryService) {
        this.userRepo = userRepo;
        this.deliveryService = deliveryService;
    }

    @GetMapping("/disponibles")
    public List<DeliveryDTO> disponiblesFIFO() {
        return userRepo.findDeliveryDisponiblesFIFO()
                .stream().map (deliveryService::toDto).toList();
    }
}
