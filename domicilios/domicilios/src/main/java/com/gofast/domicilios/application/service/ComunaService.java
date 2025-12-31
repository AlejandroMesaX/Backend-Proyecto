package com.gofast.domicilios.application.service;


import com.gofast.domicilios.domain.model.Comuna;
import com.gofast.domicilios.domain.repository.ComunaRepositoryPort;
import com.gofast.domicilios.application.dto.CrearComunaRequest;
import com.gofast.domicilios.application.exception.BadRequestException;
import com.gofast.domicilios.application.dto.EditarComunaRequest;
import com.gofast.domicilios.application.exception.NotFoundException;


import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public void crearComuna(CrearComunaRequest req) {

        // 🔐 Validaciones básicas
        if (req == null) {
            throw new BadRequestException("El cuerpo de la petición es obligatorio");
        }

        if (req.numero == null || req.numero <= 0) {
            throw new BadRequestException("El número de comuna es obligatorio y debe ser mayor a 0");
        }

        if (req.tarifaBase == null || req.tarifaBase.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("La tarifa base debe ser mayor o igual a 0");
        }

        if (req.recargoPorSalto == null || req.recargoPorSalto.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El recargo por salto debe ser mayor o igual a 0");
        }

        // ❌ No permitir comunas duplicadas por número
        if (comunaRepository.existsByNumero(req.numero)) {
            throw new BadRequestException("Ya existe una comuna con el número " + req.numero);
        }

        // ✅ Crear dominio
        Comuna comuna = new Comuna();
        comuna.setNumero(req.numero);
        comuna.setTarifaBase(req.tarifaBase);
        comuna.setRecargoPorSalto(req.recargoPorSalto);

        comunaRepository.save(comuna);
    }

    public void editarComuna(Long id, EditarComunaRequest req) {

        if (id == null) throw new BadRequestException("id es obligatorio");
        if (req == null) throw new BadRequestException("El cuerpo es obligatorio");

        Comuna comuna = comunaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comuna no encontrada"));

        // Validaciones + actualización parcial
        if (req.tarifaBase != null) {
            if (req.tarifaBase.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("tarifaBase debe ser >= 0");
            }
            comuna.setTarifaBase(req.tarifaBase);
        }

        if (req.recargoPorSalto != null) {
            if (req.recargoPorSalto.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("recargoPorSalto debe ser >= 0");
            }
            comuna.setRecargoPorSalto(req.recargoPorSalto);
        }

        comunaRepository.save(comuna);
    }
}
