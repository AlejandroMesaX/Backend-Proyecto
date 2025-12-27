package com.gofast.domicilios.application.service;

import com.gofast.domicilios.domain.model.Barrio;
import com.gofast.domicilios.domain.repository.BarrioRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicBarrioService {
    private final BarrioRepositoryPort barrioRepository;

    public PublicBarrioService(BarrioRepositoryPort barrioRepository) {
        this.barrioRepository = barrioRepository;
    }

    public List<Barrio> listarActivos() {
        return barrioRepository.findAllActivos();
    }
}
