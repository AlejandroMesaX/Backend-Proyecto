package com.gofast.domicilios.application.service;

import com.gofast.domicilios.application.dto.CrearBarrioRequest;
import com.gofast.domicilios.application.exception.BadRequestException;
import com.gofast.domicilios.domain.model.Barrio;
import com.gofast.domicilios.domain.repository.BarrioRepositoryPort;
import com.gofast.domicilios.domain.repository.ComunaRepositoryPort;
import org.springframework.stereotype.Service;
import com.gofast.domicilios.application.dto.ActualizarBarrioRequest;
import com.gofast.domicilios.application.exception.NotFoundException;
import java.util.List;

@Service
public class BarrioService {
    private final BarrioRepositoryPort barrioRepository;
    private final ComunaRepositoryPort comunaRepository;

    public BarrioService(BarrioRepositoryPort barrioRepository,
                         ComunaRepositoryPort comunaRepository) {
        this.barrioRepository = barrioRepository;
        this.comunaRepository = comunaRepository;
    }

    // ✅ CREAR (activo=true por defecto)
    public Barrio crearBarrio(CrearBarrioRequest req) {
        if (req == null) throw new BadRequestException("Body requerido");
        if (req.nombre == null || req.nombre.isBlank())
            throw new BadRequestException("El nombre del barrio es obligatorio");
        if (req.comunaNumero == null)
            throw new BadRequestException("El número de comuna es obligatorio");

        String nombre = req.nombre.trim();

        if (barrioRepository.existsActivoByNombre(nombre)) {
            throw new BadRequestException("Ya existe un barrio activo con ese nombre: " + nombre);
        }

        comunaRepository.findByNumero(req.comunaNumero)
                .orElseThrow(() -> new BadRequestException("Comuna no encontrada: " + req.comunaNumero));

        Barrio barrio = new Barrio();
        barrio.setNombre(nombre);
        barrio.setComuna(req.comunaNumero);
        barrio.setActivo(true);

        return barrioRepository.save(barrio);
    }

    // ✅ LISTAR (solo activos para el frontend)
    public List<Barrio> listarBarriosActivos() {
        return barrioRepository.findAllActivos();
    }

    // ✅ LISTAR TODOS (admin panel)
    public List<Barrio> listarTodos() {
        return barrioRepository.findAll();
    }

    // ✅ EDITAR (solo si existe)
    public Barrio editarBarrio(Long barrioId, ActualizarBarrioRequest req) {
        if (req == null) throw new BadRequestException("Body requerido");

        Barrio actual = barrioRepository.findById(barrioId)
                .orElseThrow(() -> new NotFoundException("Barrio no encontrado"));

        if (req.nombre != null && !req.nombre.isBlank()) {
            String nuevoNombre = req.nombre.trim();

            if (!nuevoNombre.equalsIgnoreCase(actual.getNombre())
                    && barrioRepository.existsActivoByNombre(nuevoNombre)) {
                throw new BadRequestException("Ya existe un barrio activo con ese nombre: " + nuevoNombre);
            }
            actual.setNombre(nuevoNombre);
        }

        if (req.comunaNumero != null) {
            comunaRepository.findByNumero(req.comunaNumero)
                    .orElseThrow(() -> new BadRequestException("Comuna no encontrada: " + req.comunaNumero));
            actual.setComuna(req.comunaNumero);
        }

        return barrioRepository.save(actual);
    }

    // ✅ DESACTIVAR (soft delete)
    public void desactivarBarrio(Long barrioId) {
        Barrio actual = barrioRepository.findById(barrioId)
                .orElseThrow(() -> new NotFoundException("Barrio no encontrado"));

        if (!actual.isActivo()) {
            throw new BadRequestException("El barrio ya está desactivado");
        }

        barrioRepository.desactivar(barrioId);
    }

    // ✅ REACTIVAR
    public void reactivarBarrio(Long barrioId) {
        Barrio actual = barrioRepository.findById(barrioId)
                .orElseThrow(() -> new NotFoundException("Barrio no encontrado"));

        if (actual.isActivo()) {
            throw new BadRequestException("El barrio ya está activo");
        }

        // si el nombre ya está en uso por otro barrio activo (edge-case)
        if (barrioRepository.existsActivoByNombre(actual.getNombre())) {
            throw new BadRequestException("Ya existe un barrio activo con ese nombre: " + actual.getNombre());
        }

        barrioRepository.reactivar(barrioId);
    }
}
