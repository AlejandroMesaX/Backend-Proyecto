package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.dto.AsignarDomiciliarioRequest;
import com.gofast.domicilios.application.dto.PageResponse;
import com.gofast.domicilios.application.dto.PedidoDTO;
import com.gofast.domicilios.application.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/pedidos")
public class AdminPedidoController {

        private final PedidoService pedidoService;

        public AdminPedidoController(PedidoService pedidoService) {
            this.pedidoService = pedidoService;
        }

        @GetMapping
        public ResponseEntity<PageResponse<PedidoDTO>> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long domiciliarioId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
        ) {
            LocalDate desdeDate = (desde == null || desde.isBlank()) ? null : LocalDate.parse(desde);
            LocalDate hastaDate = (hasta == null || hasta.isBlank()) ? null : LocalDate.parse(hasta);
            return ResponseEntity.ok(pedidoService.listarPedidos(clienteId, domiciliarioId, estado, desdeDate, hastaDate, page, size));
        }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{pedidoId}/asignar")
    public ResponseEntity<PedidoDTO> asignar(
            @PathVariable Long pedidoId,
            @RequestBody @Valid AsignarDomiciliarioRequest req
    ) {
        return ResponseEntity.ok(pedidoService.asignarPedido(pedidoId, req));
    }
}
