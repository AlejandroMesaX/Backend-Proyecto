package com.gofast.domicilios.infrastructure.rest;


import com.gofast.domicilios.application.service.ComunaService;
import com.gofast.domicilios.domain.model.Comuna;
import com.gofast.domicilios.application.dto.CrearComunaRequest;
import com.gofast.domicilios.application.service.ComunaService;
import com.gofast.domicilios.application.dto.EditarComunaRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/comunas")
public class AdminComunaController {
    private final ComunaService comunaService;

    public AdminComunaController(ComunaService comunaService) {
        this.comunaService = comunaService;
    }

    // ✅ LISTAR TODAS LAS COMUNAS (ADMIN)
    @GetMapping
    public List<Comuna> listarTodas() {
        return comunaService.listarTodas();
    }

    @PostMapping
    public ResponseEntity<Void> crear(@RequestBody CrearComunaRequest req) {
        comunaService.crearComuna(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> editar(@PathVariable Long id, @RequestBody EditarComunaRequest req) {
        comunaService.editarComuna(id, req);
        return ResponseEntity.noContent().build();
    }
}
