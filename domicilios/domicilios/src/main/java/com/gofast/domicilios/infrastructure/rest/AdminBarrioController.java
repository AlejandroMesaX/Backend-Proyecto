package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.dto.ActualizarBarrioRequest;
import com.gofast.domicilios.application.dto.CrearBarrioRequest;
import com.gofast.domicilios.application.service.BarrioService;
import com.gofast.domicilios.domain.model.Barrio;
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

    // ✅ Crear barrio (ADMIN)
    @PostMapping
    public ResponseEntity<Barrio> crear(@RequestBody CrearBarrioRequest req) {
        return ResponseEntity.ok(barrioService.crearBarrio(req));
    }

    // ✅ Listar barrios activos (para selects del frontend)
    @GetMapping
    public ResponseEntity<List<Barrio>> listarActivos() {
        return ResponseEntity.ok(barrioService.listarBarriosActivos());
    }

    // ✅ Listar TODOS (activos + inactivos) para panel admin
    @GetMapping("/todos")
    public ResponseEntity<List<Barrio>> listarTodos() {
        return ResponseEntity.ok(barrioService.listarTodos());
    }

    // ✅ Editar barrio (ADMIN)
    @PutMapping("/{barrioId}")
    public ResponseEntity<Barrio> editar(@PathVariable Long barrioId,
                                         @RequestBody ActualizarBarrioRequest req) {
        return ResponseEntity.ok(barrioService.editarBarrio(barrioId, req));
    }

    // ✅ Soft delete (desactivar)
    @DeleteMapping("/{barrioId}")
    public ResponseEntity<Void> desactivar(@PathVariable Long barrioId) {
        barrioService.desactivarBarrio(barrioId);
        return ResponseEntity.noContent().build();
    }

    // ✅ Reactivar
    @PatchMapping("/{barrioId}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable Long barrioId) {
        barrioService.reactivarBarrio(barrioId);
        return ResponseEntity.noContent().build();
    }
}
