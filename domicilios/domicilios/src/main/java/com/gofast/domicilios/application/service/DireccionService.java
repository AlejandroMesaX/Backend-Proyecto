package com.gofast.domicilios.application.service;

import com.gofast.domicilios.application.dto.*;
import com.gofast.domicilios.application.exception.*;
import com.gofast.domicilios.domain.model.Barrio;
import com.gofast.domicilios.domain.model.Direccion;
import com.gofast.domicilios.domain.repository.BarrioRepositoryPort;
import com.gofast.domicilios.domain.repository.DireccionRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.gofast.domicilios.infrastructure.security.CustomUserDetails;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DireccionService {

    private final DireccionRepositoryPort direccionRepo;
    private final BarrioRepositoryPort barrioRepo;

    public DireccionService(DireccionRepositoryPort direccionRepo,
                            BarrioRepositoryPort barrioRepo) {
        this.direccionRepo = direccionRepo;
        this.barrioRepo = barrioRepo;
    }

    @Transactional(readOnly = true)
    public List<DireccionDTO> listarMisDirecciones(Boolean activo) {
        Long clienteId = getUsuarioLogueadoId();
        return direccionRepo.findByCliente(clienteId, activo)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public DireccionDTO crear(CrearDireccionRequest req) {
        Barrio barrio = barrioRepo.findActivoByNombre(req.barrio())
                .orElseThrow(() -> {
                    log.warn(
                            "Barrio no encontrado al crear dirección. barrioNombre='{}'",
                            req.barrio());
                    return new NotFoundException(
                            "Barrio no encontrado",
                            "BARRIO_NOT_FOUND");
                });

        if (!barrio.isActivo()) {
            throw new BadRequestException(
                    "El barrio está inactivo",
                    "BARRIO_INACTIVO",
                    "barrio");
        }
        Long clienteId = getUsuarioLogueadoId();

        Direccion d = new Direccion();
        d.setClienteId(clienteId);
        d.setBarrio(req.barrio());
        d.setDireccionRecogida(req.direccionRecogida().trim());
        d.setTelefonoContacto(req.telefonoContacto().trim());
        d.setActivo(true);

        return toDTO(direccionRepo.save(d));
    }

    @Transactional
    public DireccionDTO editar(Long id, EditarDireccionRequest req) {
        Long clienteId = getUsuarioLogueadoId();

        Direccion d = direccionRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "Dirección no encontrada al editar. id='{}'",
                            id);
                    return new NotFoundException(
                            "Dirección no encontrada",
                            "DIRECCION_NOT_FOUND");
                });

        if (!d.getClienteId().equals(clienteId)) {
            log.warn(
                    "Cliente '{}' intentó editar dirección '{}' de otro cliente",
                    clienteId, id);
            throw new ForbiddenException(
                    "No tienes permiso para editar esta dirección",
                    "DIRECCION_NO_PERMITIDA");
        }

        if (req.barrioId() != null) {
            Barrio barrio = barrioRepo.findById(req.barrioId())
                    .orElseThrow(() -> {
                        log.warn("Barrio no encontrado al editar dirección. barrioId='{}'", req.barrioId());
                        return new NotFoundException("Barrio no encontrado", "BARRIO_NOT_FOUND");
                    });

            if (!barrio.isActivo()) {
                throw new BadRequestException("El barrio está inactivo", "BARRIO_INACTIVO", "barrioId");
            }
        }

        if (req.direccionRecogida() != null && !req.direccionRecogida().isBlank()) {
            d.setDireccionRecogida(req.direccionRecogida().trim());
        }

        if (req.telefonoContacto() != null && !req.telefonoContacto().isBlank()) {
            d.setTelefonoContacto(req.telefonoContacto().trim());
        }

        if (req.activo() != null) {
            d.setActivo(req.activo());
        }

        return toDTO(direccionRepo.save(d));
    }

    @Transactional
    public void eliminar(Long id) {
        Long clienteId = getUsuarioLogueadoId();

        Direccion d = direccionRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "Dirección no encontrada al eliminar. id='{}'",
                            id);
                    return new NotFoundException(
                            "Dirección no encontrada",
                            "DIRECCION_NOT_FOUND");
                });

        if (!d.getClienteId().equals(clienteId)) {
            log.warn(
                    "Cliente '{}' intentó eliminar dirección '{}' de otro cliente",
                    clienteId, id);
            throw new ForbiddenException(
                    "No tienes permiso para eliminar esta dirección",
                    "DIRECCION_NO_PERMITIDA");
        }

        d.setActivo(false);
        direccionRepo.save(d);
        direccionRepo.deleteById(id);
    }

    private DireccionDTO toDTO(Direccion d) {
        DireccionDTO dto = new DireccionDTO();
        dto.id = d.getId();
        dto.barrio = d.getBarrio();
        dto.direccionRecogida = d.getDireccionRecogida();
        dto.telefonoContacto = d.getTelefonoContacto();
        dto.activo = d.getActivo();
        return dto;
    }

    private Long getUsuarioLogueadoId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ForbiddenException(
                    "No autenticado",
                    "NO_AUTENTICADO");
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new ForbiddenException(
                    "Principal inválido",
                    "PRINCIPAL_INVALIDO");
        }

        return userDetails.getId();
    }
}
