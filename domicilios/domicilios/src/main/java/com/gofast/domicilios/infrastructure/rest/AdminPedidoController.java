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
        public ResponseEntity<List<PedidoDTO>> listarTodos() {
            return ResponseEntity.ok(pedidoService.listarTodos());
        }

        @PostMapping("/{pedidoId}/asignar")
        public ResponseEntity<PedidoDTO> asignarDomiciliario(
                @PathVariable Long pedidoId,
                @RequestBody AsignarDomiciliarioRequest req
        ) {
            PedidoDTO actualizado = pedidoService.asignarDomiciliario(pedidoId, req.domiciliarioId);
            return ResponseEntity.ok(actualizado);
        }

    @PostMapping("/{pedidoId}/estado")
    public ResponseEntity<PedidoDTO> actualizarEstadoAdmin(
            @PathVariable Long pedidoId,
            @RequestBody ActualizarEstadoPedidoRequest req
    ) {

        PedidoDTO actualizado = pedidoService.actualizarEstado(pedidoId, req.estado, null, true);
        return ResponseEntity.ok(actualizado);
    }
}
