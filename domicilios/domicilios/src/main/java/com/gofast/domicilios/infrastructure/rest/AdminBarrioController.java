package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.dto.CrearBarrioRequest;
import com.gofast.domicilios.application.service.BarrioService;
import com.gofast.domicilios.domain.model.Barrio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/barrios")
public class AdminBarrioController {
    private final BarrioService barrioService;

    public AdminBarrioController(BarrioService barrioService) {
        this.barrioService = barrioService;
    }

    // ✅ Crear barrio (ADMIN)
    @PostMapping
    public ResponseEntity<Barrio> crear(@RequestBody CrearBarrioRequest req) {
        Barrio creado = barrioService.crearBarrio(req);
        return ResponseEntity.ok(creado);
    }
}
