package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.service.DeliveryPedidosService;
import com.gofast.domicilios.application.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delivery/pedidos")
public class DeliveryPedidosController {
    private final DeliveryPedidosService service;

    public DeliveryPedidosController(DeliveryPedidosService service) {
        this.service = service;
    }

    @PatchMapping("/{pedidoId}/recogido")
    public ResponseEntity<Void> marcarRecogido(
            @PathVariable Long pedidoId,
            @AuthenticationPrincipal UserDetails user
    ) {
        service.marcarRecogido(pedidoId, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{pedidoId}/entregado")
    public ResponseEntity<Void> marcarEntregado(
            @PathVariable Long pedidoId,
            @AuthenticationPrincipal UserDetails user
    ) {
        service.marcarEntregado(pedidoId, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
