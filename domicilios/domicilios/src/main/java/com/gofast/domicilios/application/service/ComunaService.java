package com.gofast.domicilios.application.service;


import com.gofast.domicilios.domain.model.Comuna;
import com.gofast.domicilios.domain.repository.ComunaRepositoryPort;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComunaService {
    private final ComunaRepositoryPort comunaRepository;

    public ComunaService(ComunaRepositoryPort comunaRepository) {
        this.comunaRepository = comunaRepository;
    }

    // ✅ LISTAR TODAS (ADMIN)
    public List<Comuna> listarTodas() {
        return comunaRepository.findAll();
    }
}
