package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.service.ComunaService;
import com.gofast.domicilios.domain.model.Comuna;
import com.gofast.domicilios.application.dto.CrearComunaRequest;
import com.gofast.domicilios.application.dto.EditarComunaRequest;
import com.gofast.domicilios.application.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comunas")
public class AdminComunaController {
    private final ComunaService comunaService;

    public AdminComunaController(ComunaService comunaService) {
        this.comunaService = comunaService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<Comuna>> listarTodas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(comunaService.listarTodas(page, size));
    }

    @PostMapping
    public ResponseEntity<Void> crear(@RequestBody @Valid CrearComunaRequest req) {
        comunaService.crearComuna(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> editar(@PathVariable Long id,
                                       @RequestBody @Valid EditarComunaRequest req) {
        comunaService.editarComuna(id, req);
        return ResponseEntity.ok().build();
    }
}
