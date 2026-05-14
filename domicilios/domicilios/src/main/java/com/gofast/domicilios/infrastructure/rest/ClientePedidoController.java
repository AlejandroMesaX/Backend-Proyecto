package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.dto.CrearPedidoRequest;
import com.gofast.domicilios.application.dto.PageResponse;
import com.gofast.domicilios.application.dto.PedidoDTO;
import com.gofast.domicilios.application.service.PedidoService;
import com.gofast.domicilios.infrastructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/cliente/pedidos")
public class ClientePedidoController {
    private final PedidoService pedidoService;

    public ClientePedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoDTO> crearPedido(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                 @RequestBody @Valid CrearPedidoRequest req) {
        Long clienteId = currentUser.getId();
        PedidoDTO creado = pedidoService.crearPedidoParaCliente(clienteId, req);
        return ResponseEntity.ok(creado);
    }

    @GetMapping
    public ResponseEntity<PageResponse<PedidoDTO>> misPedidos(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long clienteId = currentUser.getId();
        LocalDate desdeDate = (desde == null || desde.isBlank()) ? null : LocalDate.parse(desde);
        LocalDate hastaDate = (hasta == null || hasta.isBlank()) ? null : LocalDate.parse(hasta);

        return ResponseEntity.ok(pedidoService.listarPedidosDelCliente(clienteId, desdeDate, hastaDate, page, size));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> cancelarPropioPedido(@PathVariable Long id) {
        pedidoService.cancelarPedidoPorCliente(id);
        return ResponseEntity.noContent().build();
    }
}
