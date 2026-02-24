package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.dto.CrearPedidoRequest;
import com.gofast.domicilios.application.dto.PedidoDTO;
import com.gofast.domicilios.application.service.PedidoService;
import com.gofast.domicilios.infrastructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cliente/pedidos")
public class ClientePedidoController {
    private final PedidoService pedidoService;

    public ClientePedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // Crear pedido cliente
    @PostMapping
    public ResponseEntity<PedidoDTO> crearPedido(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                 @RequestBody @Valid CrearPedidoRequest req) {
        Long clienteId = currentUser.getId();
        PedidoDTO creado = pedidoService.crearPedidoParaCliente(clienteId, req);
        return ResponseEntity.ok(creado);
    }

    @GetMapping("/mios")
    public ResponseEntity<List<PedidoDTO>> listarMisPedidos(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long clienteId = currentUser.getId();
        List<PedidoDTO> pedidos = pedidoService.listarPorCliente(clienteId);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping
    public ResponseEntity<List<PedidoDTO>> misPedidos(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ) {
        LocalDate desdeDate = (desde == null || desde.isBlank()) ? null : LocalDate.parse(desde);
        LocalDate hastaDate = (hasta == null || hasta.isBlank()) ? null : LocalDate.parse(hasta);

        return ResponseEntity.ok(pedidoService.listarPedidosDelCliente(desdeDate, hastaDate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarPropioPedido(@PathVariable Long id) {
        pedidoService.cancelarPedidoPorCliente(id);
        return ResponseEntity.noContent().build();
    }
}
