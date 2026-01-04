package com.gofast.domicilios.application.service;


import com.gofast.domicilios.application.dto.*;
import com.gofast.domicilios.application.exception.*;
import com.gofast.domicilios.domain.model.Barrio;
import com.gofast.domicilios.domain.model.Direccion;
import com.gofast.domicilios.domain.repository.BarrioRepositoryPort;
import com.gofast.domicilios.domain.repository.DireccionRepositoryPort;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.gofast.domicilios.infrastructure.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
public class DireccionService {

    private final DireccionRepositoryPort direccionRepo;
    private final BarrioRepositoryPort barrioRepo;

    public DireccionService(DireccionRepositoryPort direccionRepo, BarrioRepositoryPort barrioRepo) {
        this.direccionRepo = direccionRepo;
        this.barrioRepo = barrioRepo;
    }

    public List<DireccionDTO> listarMisDirecciones(Boolean activo) {
        Long clienteId = getUsuarioLogueadoId();
        return direccionRepo.findByCliente(clienteId, activo)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public DireccionDTO crear(CrearDireccionRequest req) {
        if (req == null) throw new BadRequestException("El body es obligatorio");
        if (req.barrioId == null) throw new BadRequestException("barrioId es obligatorio");
        if (req.direccionRecogida == null || req.direccionRecogida.isBlank())
            throw new BadRequestException("direccionRecogida es obligatoria");
        if (req.telefonoContacto == null || req.telefonoContacto.isBlank())
            throw new BadRequestException("telefonoContacto es obligatorio");

        // Validar barrio existe y activo
        Barrio barrio = barrioRepo.findById(req.barrioId)
                .orElseThrow(() -> new BadRequestException("El barrio no existe"));
        if (!Boolean.TRUE.equals(barrio.isActivo())) {
            throw new BadRequestException("El barrio está inactivo");
        }

        Long clienteId = getUsuarioLogueadoId();

        Direccion d = new Direccion();
        d.setClienteId(clienteId);
        d.setBarrioId(req.barrioId);
        d.setDireccionRecogida(req.direccionRecogida.trim());
        d.setTelefonoContacto(req.telefonoContacto.trim());
        d.setActivo(true);

        return toDTO(direccionRepo.save(d));
    }

    public DireccionDTO editar(Long id, EditarDireccionRequest req) {
        if (id == null) throw new BadRequestException("id es obligatorio");
        if (req == null) throw new BadRequestException("El body es obligatorio");

        Long clienteId = getUsuarioLogueadoId();

        Direccion d = direccionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Dirección no encontrada"));

        if (!d.getClienteId().equals(clienteId)) {
            throw new ForbiddenException("No puedes editar direcciones de otro cliente");
        }

        if (req.barrioId != null) {
            Barrio barrio = barrioRepo.findById(req.barrioId)
                    .orElseThrow(() -> new BadRequestException("El barrio no existe"));
            if (!Boolean.TRUE.equals(barrio.isActivo())) {
                throw new BadRequestException("El barrio está inactivo");
            }
            d.setBarrioId(req.barrioId);
        }

        if (req.direccionRecogida != null && !req.direccionRecogida.isBlank()) {
            d.setDireccionRecogida(req.direccionRecogida.trim());
        }

        if (req.telefonoContacto != null && !req.telefonoContacto.isBlank()) {
            d.setTelefonoContacto(req.telefonoContacto.trim());
        }

        if (req.activo != null) {
            d.setActivo(req.activo);
        }

        return toDTO(direccionRepo.save(d));
    }

    public void eliminar(Long id) {
        if (id == null) throw new BadRequestException("id es obligatorio");

        Long clienteId = getUsuarioLogueadoId();

        Direccion d = direccionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Dirección no encontrada"));

        if (!d.getClienteId().equals(clienteId)) {
            throw new ForbiddenException("No puedes eliminar direcciones de otro cliente");
        }

        d.setActivo(false);
        direccionRepo.save(d);
    }

    // ---- mapper DTO ----
    private DireccionDTO toDTO(Direccion d) {
        DireccionDTO dto = new DireccionDTO();
        dto.id = d.getId();
        dto.barrioId = d.getBarrioId();
        dto.direccionRecogida = d.getDireccionRecogida();
        dto.telefonoContacto = d.getTelefonoContacto();
        dto.activo = d.getActivo();
        return dto;
    }

    // ---- obtener usuario logueado ----
    private Long getUsuarioLogueadoId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ForbiddenException("No autenticado");
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new ForbiddenException("Principal inválido");
        }

        return userDetails.getId();
    }
}
