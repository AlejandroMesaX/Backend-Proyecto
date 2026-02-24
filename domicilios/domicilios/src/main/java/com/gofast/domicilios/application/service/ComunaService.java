package com.gofast.domicilios.application.service;


import com.gofast.domicilios.domain.model.Comuna;
import com.gofast.domicilios.domain.repository.ComunaRepositoryPort;
import com.gofast.domicilios.application.dto.CrearComunaRequest;
import com.gofast.domicilios.application.exception.BadRequestException;
import com.gofast.domicilios.application.dto.EditarComunaRequest;
import com.gofast.domicilios.application.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ComunaService {
    private final ComunaRepositoryPort comunaRepository;
    //private static final Logger log = LoggerFactory.getLogger(ComunaService.class);

    public ComunaService(ComunaRepositoryPort comunaRepository) {
        this.comunaRepository = comunaRepository;
    }

    @Transactional(readOnly = true)
    public List<Comuna> listarTodas() {
        return comunaRepository.findAll();
    }

    @Transactional
    public void crearComuna(CrearComunaRequest req) {
        if (comunaRepository.existsByNumero(req.numero())) {
            log.warn(
                    "Intento de crear comuna duplicada. numero='{}'",
                    req.numero());
            throw new BadRequestException(
                    "Ya existe una comuna con ese número",
                    "COMUNA_DUPLICADA",
                    "numero");
        }

        Comuna comuna = new Comuna();
        comuna.setNumero(req.numero());
        comuna.setTarifaBase(req.tarifaBase());
        comuna.setRecargoPorSalto(req.recargoPorSalto());

        try {
            comunaRepository.save(comuna);
        } catch (DataIntegrityViolationException e) {
            log.warn(
                    "Comuna duplicada detectada por constraint de BD. numero='{}'",
                    req.numero());
            throw new BadRequestException(
                    "Ya existe una comuna con ese número",
                    "COMUNA_DUPLICADA", "numero");
        }
    }

    @Transactional
    public void editarComuna(Long id, EditarComunaRequest req) {
        Comuna comuna = comunaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "Comuna no encontrada al editar. id='{}'",
                            id);
                    return new NotFoundException(
                            "Comuna no encontrada",
                            "COMUNA_NOT_FOUND");
                });

        if (req.tarifaBase() != null) {
            comuna.setTarifaBase(req.tarifaBase());
        }

        if (req.recargoPorSalto() != null) {
            comuna.setRecargoPorSalto(req.recargoPorSalto());
        }

        comunaRepository.save(comuna);
    }
}
