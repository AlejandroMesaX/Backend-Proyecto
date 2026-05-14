package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.dto.PedidoDTO;
import com.gofast.domicilios.application.dto.ReportarIncidenciaRequest;
import com.gofast.domicilios.application.service.PedidoService;
import com.gofast.domicilios.domain.model.EstadoPedido;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/domiciliario/pedidos")
public class DomiciliarioPedidoController {

    private final PedidoService pedidoService;

    public DomiciliarioPedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public ResponseEntity<List<PedidoDTO>> misPedidos(
            Authentication authentication,
            @RequestParam(required = false) EstadoPedido estado
    ) {
        return ResponseEntity.ok(
                pedidoService.listarPedidosDelDomiciliario(authentication, estado)
        );
    }

    @PatchMapping("/{pedidoId}/estado")
    public ResponseEntity<PedidoDTO> cambiarEstado(
            Authentication authentication,
            @PathVariable Long pedidoId
    ) {
        return ResponseEntity.ok(
                pedidoService.cambiarEstadoComoDomiciliario(authentication, pedidoId)
        );
    }

    @PatchMapping("/{pedidoId}/ayuda")
    public ResponseEntity<PedidoDTO> pedirAyuda(
            Authentication authentication,
            @PathVariable Long pedidoId,
            @RequestBody @Valid ReportarIncidenciaRequest req
    ) {
        return ResponseEntity.ok(
                pedidoService.reportarIncidenciaComoDomiciliario(authentication, pedidoId, req)
        );
    }

    @GetMapping("/me/entregados")
    public ResponseEntity<List<PedidoDTO>> historialEntregados(Authentication authentication) {
        return ResponseEntity.ok(pedidoService.misPedidosEntregadosComoDomiciliario(authentication));
    }

    @GetMapping("/me/activos")
    public ResponseEntity<List<PedidoDTO>> pedidosActivos(Authentication authentication) {
        return ResponseEntity.ok(pedidoService.misPedidosActivos(authentication));
    }
}
