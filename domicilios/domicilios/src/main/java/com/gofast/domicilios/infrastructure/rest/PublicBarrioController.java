package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.service.PublicBarrioService;
import com.gofast.domicilios.domain.model.Barrio;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/barrios")
public class PublicBarrioController {
    private final PublicBarrioService service;

    public PublicBarrioController(PublicBarrioService service) {
        this.service = service;
    }

    // ✅ LISTAR BARRIOS ACTIVOS (PÚBLICO)
    @GetMapping
    public List<Barrio> listar() {
        return service.listarActivos();
    }
}
