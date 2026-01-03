package com.gofast.domicilios.application.service;

import com.gofast.domicilios.application.dto.EditarUsuarioRequest;
import com.gofast.domicilios.application.dto.RegisterUsuarioRequest;
import com.gofast.domicilios.application.dto.UsuarioDTO;
import com.gofast.domicilios.application.exception.ForbiddenException;
import com.gofast.domicilios.domain.model.Rol;
import com.gofast.domicilios.domain.model.Usuario;
import com.gofast.domicilios.application.exception.NotFoundException;
import com.gofast.domicilios.domain.repository.UsuarioRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.gofast.domicilios.application.exception.BadRequestException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {
    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepositoryPort usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioDTO registrarUsuario(RegisterUsuarioRequest req) {
        usuarioRepository.findByEmail(req.email)
                .ifPresent(u -> {
                    throw new RuntimeException("El email ya está registrado");
                });

        Usuario usuario = new Usuario();
        usuario.setNombre(req.nombre);
        usuario.setEmail(req.email);
        usuario.setPasswordHash(passwordEncoder.encode(req.password));
        usuario.setRol(Rol.valueOf(req.rol));
        usuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(usuario);
        return toDTO(guardado);
    }

    public UsuarioDTO obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return toDTO(usuario);
    }

    public void desactivarUsuario(Long usuarioId, Long adminId) {
        if (adminId != null && adminId.equals(usuarioId)) {
            throw new BadRequestException("No puedes desactivar tu propio usuario (admin).");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    public void reactivarUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    public void editarUsuario(Long id, EditarUsuarioRequest req) {

        if (id == null) {
            throw new BadRequestException("El id es obligatorio");
        }

        if (req == null) {
            throw new BadRequestException("El cuerpo de la petición es obligatorio");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // 🔒 Regla: no permitir desactivar al ADMIN logueado (si ya la tienes, respétala)
        if (req.activo != null && !req.activo && usuario.getRol() == Rol.ADMIN) {
            throw new ForbiddenException("No se puede desactivar un usuario ADMIN");
        }

        // ✏️ Nombre
        if (req.nombre != null && !req.nombre.isBlank()) {
            usuario.setNombre(req.nombre.trim());
        }

        // 🎭 Rol
        if (req.rol != null) {
            try {
                Rol nuevoRol = Rol.valueOf(req.rol.toUpperCase());
                usuario.setRol(nuevoRol);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Rol inválido");
            }
        }

        // 🔄 Activo / Inactivo
        if (req.activo != null) {
            usuario.setActivo(req.activo);
        }

        usuarioRepository.save(usuario);
    }

    public List<UsuarioDTO> listarUsuarios(String nombre, String rol, Boolean activo) {

        Rol rolEnum = null;
        if (rol != null && !rol.isBlank()) {
            try {
                rolEnum = Rol.valueOf(rol.trim().toUpperCase());
            } catch (Exception e) {
                throw new BadRequestException("Rol inválido: " + rol + ". Use ADMIN, CLIENT o DELIVERY.");
            }
        }

        List<Usuario> usuarios = usuarioRepository.findByFiltros(
                (nombre == null || nombre.isBlank()) ? null : nombre.trim(),
                rolEnum,
                activo
        );

        return usuarios.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public UsuarioDTO obtenerUsuarioPorId(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        return toDTO(usuario);
    }

    public UsuarioDTO toDTO(Usuario u) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.id = u.getId();
        dto.nombre = u.getNombre();
        dto.email = u.getEmail();
        dto.rol = u.getRol().name();
        dto.activo = u.isActivo();
        return dto;
    }
}

