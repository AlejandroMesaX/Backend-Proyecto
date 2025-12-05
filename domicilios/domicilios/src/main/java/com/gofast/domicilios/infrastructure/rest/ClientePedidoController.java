package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.dto.CrearPedidoRequest;
import com.gofast.domicilios.application.dto.PedidoDTO;
import com.gofast.domicilios.application.service.PedidoService;
import com.gofast.domicilios.infrastructure.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                                                 @RequestBody CrearPedidoRequest req) {
        Long clienteId = currentUser.getId();
        PedidoDTO creado = pedidoService.crearPedidoParaCliente(clienteId, req);
        return ResponseEntity.ok(creado);
    }

    // Listar los pedidos
    @GetMapping("/mios")
    public ResponseEntity<List<PedidoDTO>> listarMisPedidos(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long clienteId = currentUser.getId();
        List<PedidoDTO> pedidos = pedidoService.listarPorCliente(clienteId);
        return ResponseEntity.ok(pedidos);
    }

    @PostMapping("/{pedidoId}/cancelar")
    public ResponseEntity<PedidoDTO> cancelarPedido(
            @PathVariable Long pedidoId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long clienteId = currentUser.getId();
        PedidoDTO actualizado = pedidoService.cancelarPedidoPorCliente(pedidoId, clienteId);
        return ResponseEntity.ok(actualizado);
    }
}
