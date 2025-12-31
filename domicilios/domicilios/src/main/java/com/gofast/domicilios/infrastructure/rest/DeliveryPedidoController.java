package com.gofast.domicilios.infrastructure.rest;


import com.gofast.domicilios.application.dto.ActualizarEstadoPedidoRequest;
import com.gofast.domicilios.application.dto.PedidoDTO;
import com.gofast.domicilios.application.service.PedidoService;
import com.gofast.domicilios.infrastructure.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery/pedidos")
public class DeliveryPedidoController {

    private final PedidoService pedidoService;

    public DeliveryPedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/mios")
    public ResponseEntity<List<PedidoDTO>> listarMisPedidos(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long domiciliarioId = currentUser.getId();
        return ResponseEntity.ok(pedidoService.listarPorDomiciliario(domiciliarioId));
    }
}
