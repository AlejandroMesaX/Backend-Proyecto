package com.gofast.domicilios.application.service;

import com.gofast.domicilios.application.dto.CrearBarrioRequest;
import com.gofast.domicilios.application.exception.BadRequestException;
import com.gofast.domicilios.domain.model.Barrio;
import com.gofast.domicilios.domain.repository.BarrioRepositoryPort;
import com.gofast.domicilios.domain.repository.ComunaRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class BarrioService {
    private final BarrioRepositoryPort barrioRepository;
    private final ComunaRepositoryPort comunaRepository;

    public BarrioService(BarrioRepositoryPort barrioRepository,
                         ComunaRepositoryPort comunaRepository) {
        this.barrioRepository = barrioRepository;
        this.comunaRepository = comunaRepository;
    }

    public Barrio crearBarrio(CrearBarrioRequest req) {
        if (req == null) throw new BadRequestException("Body requerido");
        if (req.nombre == null || req.nombre.isBlank()) {
            throw new BadRequestException("El nombre del barrio es obligatorio");
        }
        if (req.comunaNumero == null) {
            throw new BadRequestException("El número de comuna es obligatorio");
        }

        // evitar duplicados
        if (barrioRepository.existsByNombre(req.nombre.trim())) {
            throw new BadRequestException("Ya existe un barrio con ese nombre: " + req.nombre);
        }

        // validar que la comuna exista (usando el port de dominio)
        comunaRepository.findByNumero(req.comunaNumero)
                .orElseThrow(() -> new BadRequestException("Comuna no encontrada: " + req.comunaNumero));

        Barrio barrio = new Barrio();
        barrio.setNombre(req.nombre.trim());
        barrio.setComuna(req.comunaNumero);

        return barrioRepository.save(barrio);
    }
}
