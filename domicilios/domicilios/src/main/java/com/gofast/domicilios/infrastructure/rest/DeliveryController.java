package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.dto.DeliveryStatusDTO;
import com.gofast.domicilios.application.dto.DisponibilidadRequest;
import com.gofast.domicilios.application.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {
    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PatchMapping("/me/disponibilidad")
    public ResponseEntity<Void> setDisponibilidad(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody DisponibilidadRequest req
    ) {
        deliveryService.setDisponible(user.getUsername(), req.disponible());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<DeliveryStatusDTO> getMe(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(deliveryService.getEstado(user.getUsername()));
    }
}
