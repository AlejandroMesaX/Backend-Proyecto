package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.dto.AsignarDomiciliarioRequest;
import com.gofast.domicilios.application.dto.PedidoDTO;
import com.gofast.domicilios.application.service.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gofast.domicilios.application.dto.ActualizarEstadoPedidoRequest;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pedidos")
public class AdminPedidoController {

        private final PedidoService pedidoService;

        public AdminPedidoController(PedidoService pedidoService) {
            this.pedidoService = pedidoService;
        }

        @GetMapping
        public ResponseEntity<List<PedidoDTO>> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long domiciliarioId
        ) {
        return ResponseEntity.ok(pedidoService.listarPedidos(clienteId, domiciliarioId));
        }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{pedidoId}/asignar")
    public ResponseEntity<PedidoDTO> asignar(
            @PathVariable Long pedidoId,
            @RequestBody AsignarDomiciliarioRequest req
    ) {
        return ResponseEntity.ok(pedidoService.asignarPedido(pedidoId, req));
    }
}
