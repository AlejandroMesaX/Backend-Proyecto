package com.gofast.domicilios.application.service;

import com.gofast.domicilios.application.dto.BarrioDTO;
import com.gofast.domicilios.application.dto.CrearBarrioRequest;
import com.gofast.domicilios.application.exception.BadRequestException;
import com.gofast.domicilios.domain.model.Barrio;
import com.gofast.domicilios.domain.model.Comuna;
import com.gofast.domicilios.domain.repository.BarrioRepositoryPort;
import com.gofast.domicilios.domain.repository.ComunaRepositoryPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import com.gofast.domicilios.application.dto.ActualizarBarrioRequest;
import com.gofast.domicilios.application.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BarrioService {

    private final BarrioRepositoryPort barrioRepository;
    private final ComunaRepositoryPort comunaRepository;
    private static final Logger log = LoggerFactory.getLogger(BarrioService.class);

    public BarrioService(BarrioRepositoryPort barrioRepository,
                         ComunaRepositoryPort comunaRepository) {
        this.barrioRepository = barrioRepository;
        this.comunaRepository = comunaRepository;
    }

    @Transactional
    public Barrio crearBarrio(CrearBarrioRequest req) {
        String nombre = req.nombre().trim();

        if (barrioRepository.existsActivoByNombre(nombre)) {
            log.warn(
                    "Intento de crear barrio duplicado. nombre='{}'",
                    nombre);
            throw new BadRequestException(
                    "Ya existe un barrio activo con ese nombre",
                    "BARRIO_DUPLICADO",
                    "nombre");
        }

        Optional<Comuna> comuna = comunaRepository.findByNumero(req.comunaNumero());
        if (comuna.isEmpty()) {
            log.warn(
                    "Comuna no encontrada al crear barrio. comunaNumero='{}'",
                    req.comunaNumero());
            throw new NotFoundException(
                    "Comuna no encontrada",
                    "COMUNA_NOT_FOUND");
        }

        Barrio barrio = new Barrio();
        barrio.setNombre(req.nombre());
        barrio.setComuna(req.comunaNumero());
        barrio.setActivo(true);

        try {
            return barrioRepository.save(barrio);
        } catch (DataIntegrityViolationException e) {
            log.warn(
                    "Barrio duplicado detectado. nombre='{}'",
                    req.nombre());
            throw new BadRequestException(
                    "Ya existe un barrio activo con ese nombre",
                    "BARRIO_DUPLICADO", "nombre");
        }
    }

    public List<BarrioDTO> listarBarrios(String nombre, Integer comunaNumero, Boolean activo) {
        return barrioRepository.findByFiltros(
                        (nombre == null || nombre.isBlank()) ? null : nombre.trim(),
                        comunaNumero,
                        activo
                )
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public Barrio editarBarrio(Long barrioId, ActualizarBarrioRequest req) {
        Barrio actual = barrioRepository.findById(barrioId)
                .orElseThrow(() -> {
                    log.warn(
                            "Barrio no encontrado al editar. barrioId='{}'",
                            barrioId);
                    return new NotFoundException(
                            "Barrio no encontrado",
                            "BARRIO_NOT_FOUND");
                });

        if (req.nombre() != null && !req.nombre().isBlank()) {
            String nuevoNombre = req.nombre().trim();

            if (!nuevoNombre.equalsIgnoreCase(actual.getNombre())
                    && barrioRepository.existsActivoByNombre(nuevoNombre)) {
                log.warn(
                        "Intento de editar barrio duplicado. nombre='{}'",
                        nuevoNombre);
                throw new BadRequestException(
                        "Ya existe un barrio activo con ese nombre",
                        "BARRIO_DUPLICADO",
                        "nombre"
                );
            }
            actual.setNombre(nuevoNombre);
        }

        if (req.comunaNumero() != null) {
            Optional<Comuna> comuna = comunaRepository.findByNumero(req.comunaNumero());
            if (comuna.isEmpty()) {
                log.warn(
                        "Comuna no encontrada al editar barrio. comunaNumero='{}'",
                        req.comunaNumero());
                throw new NotFoundException(
                        "Comuna no encontrada",
                        "COMUNA_NOT_FOUND");
            }
            actual.setComuna(req.comunaNumero());
        }

        return barrioRepository.save(actual);
    }

    @Transactional
    public void desactivarBarrio(Long barrioId) {
        Barrio actual = barrioRepository.findById(barrioId)
                .orElseThrow(() -> {
                    log.warn(
                            "Barrio no encontrado al desactivar. barrioId='{}'",
                            barrioId);
                    return new NotFoundException(
                            "Barrio no encontrado",
                            "BARRIO_NOT_FOUND");
                });

        if (!actual.isActivo()) {
            log.warn(
                    "Intento de desactivar barrio. nombre='{}'",
                    actual.getNombre());
            throw new BadRequestException(
                    "El barrio ya esta desactivado",
                    "BARRIO_DESACTIVADO",
                    "barrio"
            );
        }

        barrioRepository.desactivar(barrioId);
    }

    @Transactional
    public void reactivarBarrio(Long barrioId) {
        Barrio actual = barrioRepository.findById(barrioId)
                .orElseThrow(() -> {
                    log.warn(
                            "Barrio no encontrado al reactivar. barrioId='{}'",
                            barrioId);
                    return new NotFoundException(
                            "Barrio no encontrado",
                            "BARRIO_NOT_FOUND");
                });

        if (actual.isActivo()) {
            log.warn(
                    "Intento de reactivar barrio. nombre='{}'",
                    actual.getNombre());
            throw new BadRequestException(
                    "El barrio ya esta activo",
                    "BARRIO_ACTIVADO",
                    "barrio"
            );
        }

        barrioRepository.reactivar(barrioId);
    }

    private BarrioDTO toDTO(Barrio b) {
        BarrioDTO dto = new BarrioDTO();
        dto.id = b.getId();
        dto.nombre = b.getNombre();
        dto.activo = b.isActivo();
        dto.comuna = b.getComuna();
        return dto;
    }
}
