package com.gofast.domicilios.infrastructure.rest;


import com.gofast.domicilios.application.service.ComunaService;
import com.gofast.domicilios.domain.model.Comuna;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
