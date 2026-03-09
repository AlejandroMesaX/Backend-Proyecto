package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.dto.TarifaResponse;
import com.gofast.domicilios.domain.service.TarifaDomicilioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/tarifas")
public class TarifaController {

    private final TarifaDomicilioService tarifaService;

    public TarifaController(TarifaDomicilioService tarifaService) {
        this.tarifaService = tarifaService;
    }

    @GetMapping("/calcular")
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY', 'CLIENT')")
    public ResponseEntity<TarifaResponse> calcular(
            @RequestParam String barrioRecogida,
            @RequestParam String barrioEntrega
    ) {
        BigDecimal costo = tarifaService.calcularCosto(barrioRecogida, barrioEntrega);
        return ResponseEntity.ok(new TarifaResponse(barrioRecogida, barrioEntrega, costo));
    }
}