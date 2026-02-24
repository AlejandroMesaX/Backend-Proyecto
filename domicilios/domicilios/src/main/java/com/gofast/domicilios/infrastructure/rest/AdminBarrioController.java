package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.dto.ActualizarBarrioRequest;
import com.gofast.domicilios.application.dto.CrearBarrioRequest;
import com.gofast.domicilios.application.service.BarrioService;
import com.gofast.domicilios.domain.model.Barrio;
import com.gofast.domicilios.application.dto.BarrioDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/barrios")
public class AdminBarrioController {
    private final BarrioService barrioService;

    public AdminBarrioController(BarrioService barrioService) {
        this.barrioService = barrioService;
    }

    @PostMapping
    public ResponseEntity<Barrio> crear(@RequestBody @Valid CrearBarrioRequest req) {
        return ResponseEntity.ok(barrioService.crearBarrio(req));
    }

    @GetMapping
    public ResponseEntity<List<BarrioDTO>> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Integer comuna,
            @RequestParam(required = false) Boolean activo
    ) {
        return ResponseEntity.ok(barrioService.listarBarrios(nombre, comuna, activo));
    }

    @PutMapping("/{barrioId}")
    public ResponseEntity<Barrio> editar(@PathVariable Long barrioId,
                                         @RequestBody @Valid ActualizarBarrioRequest req) {
        return ResponseEntity.ok(barrioService.editarBarrio(barrioId, req));
    }

    @PatchMapping("/{barrioId}/deshabilitar")
    public ResponseEntity<Void> desactivar(@PathVariable Long barrioId) {
        barrioService.desactivarBarrio(barrioId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{barrioId}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable Long barrioId) {
        barrioService.reactivarBarrio(barrioId);
        return ResponseEntity.noContent().build();
    }
}
