package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.dto.*;
import com.gofast.domicilios.application.service.DireccionService;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cliente/direcciones")
public class ClienteDireccionController {

    private final DireccionService direccionService;

    public ClienteDireccionController(DireccionService direccionService) {
        this.direccionService = direccionService;
    }

    @GetMapping
    public ResponseEntity<List<DireccionDTO>> listar(@RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(direccionService.listarMisDirecciones(activo));
    }

    @PostMapping
    public ResponseEntity<DireccionDTO> crear(@RequestBody @Valid CrearDireccionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(direccionService.crear(req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DireccionDTO> editar(@PathVariable Long id, @RequestBody @Valid EditarDireccionRequest req) {
        return ResponseEntity.ok(direccionService.editar(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        direccionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
