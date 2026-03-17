package com.gofast.domicilios.application.service;

import com.gofast.domicilios.application.dto.EditarUsuarioRequest;
import com.gofast.domicilios.application.dto.LoginResponse;
import com.gofast.domicilios.application.dto.RegisterUsuarioRequest;
import com.gofast.domicilios.application.dto.UsuarioDTO;
import com.gofast.domicilios.application.exception.ForbiddenException;
import com.gofast.domicilios.domain.model.EstadoDelivery;
import com.gofast.domicilios.domain.model.Rol;
import com.gofast.domicilios.domain.model.Usuario;
import com.gofast.domicilios.application.exception.NotFoundException;
import com.gofast.domicilios.domain.repository.UsuarioRepositoryPort;
import com.gofast.domicilios.infrastructure.realtime.RealtimePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.gofast.domicilios.application.exception.BadRequestException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.gofast.domicilios.application.service.EmailService;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
public class UsuarioService {
    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RealtimePublisher realtimePublisher;
    private final EmailService emailService;

    public UsuarioService(UsuarioRepositoryPort usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          RealtimePublisher realtimePublisher,
                          EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.realtimePublisher = realtimePublisher;
        this.emailService = emailService;
    }

    @Transactional
    public UsuarioDTO registrarUsuario(RegisterUsuarioRequest req) {
        // Validar email único
        if (usuarioRepository.findByEmail(req.email()).isPresent()) {
            throw new RuntimeException("Ya existe una cuenta con ese correo.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(req.nombre());
        usuario.setEmail(req.email());
        usuario.setPasswordHash(passwordEncoder.encode(req.password()));
        usuario.setRol(Rol.valueOf(req.rol()));
        usuario.setActivo(false);           // inactivo hasta verificar
        usuario.setEmailVerificado(false);
        usuario.setEstadoDelivery(EstadoDelivery.DESCONECTADO); // ← agregar esta línea

        // Generar código de 6 dígitos
        String codigo = generarCodigo();
        usuario.setCodigoVerificacion(codigo);
        usuario.setCodigoExpiracion(LocalDateTime.now().plusMinutes(15));

        Usuario guardado = usuarioRepository.save(usuario);

        // Enviar correo
        emailService.enviarCodigoVerificacion(usuario.getEmail(), usuario.getNombre(), codigo);

        return toDTO(guardado);
    }

    public Usuario verificarYActivar(String email, String code) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (usuario.isEmailVerificado())
            throw new RuntimeException("Esta cuenta ya fue verificada.");
        if (!usuario.getCodigoVerificacion().equals(code))
            throw new RuntimeException("Código incorrecto.");
        if (LocalDateTime.now().isAfter(usuario.getCodigoExpiracion()))
            throw new RuntimeException("El código expiró. Solicita uno nuevo.");

        usuario.setEmailVerificado(true);
        usuario.setActivo(true);
        usuario.setCodigoVerificacion(null);
        usuario.setCodigoExpiracion(null);
        return usuarioRepository.save(usuario);
    }

    public void reenviarCodigo(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (usuario.isEmailVerificado()) {
            throw new RuntimeException("Esta cuenta ya fue verificada.");
        }

        String codigo = generarCodigo();
        usuario.setCodigoVerificacion(codigo);
        usuario.setCodigoExpiracion(LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(usuario);

        emailService.enviarCodigoVerificacion(usuario.getEmail(), usuario.getNombre(), codigo);
    }

    private String generarCodigo() {
        SecureRandom random = new SecureRandom();
        int numero = random.nextInt(900000) + 100000; // 100000–999999
        return String.valueOf(numero);
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(this::toDTO)
                .orElseThrow(() -> {
                    log.warn(
                            "Usuario no encontrado por email");
                    return new NotFoundException(
                            "Usuario no encontrado",
                            "USUARIO_NOT_FOUND");
                });
    }

    @Transactional(readOnly = true)
    public Usuario obtenerEntidadPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        "Usuario no encontrado",
                        "USUARIO_NOT_FOUND"));
    }

    @Transactional
    public void desactivarUsuario(Long usuarioId, Long adminId) {
        if (adminId != null && adminId.equals(usuarioId)) {
            throw new BadRequestException(
                    "No puedes desactivar tu propio usuario",
                    "ADMIN_SELF_DESACTIVAR",
                    "usuarioId");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> {
                    log.warn(
                            "Usuario no encontrado al desactivar. usuarioId='{}'",
                            usuarioId);
                    return new NotFoundException(
                            "Usuario no encontrado",
                            "USUARIO_NOT_FOUND");
                });

        if (usuario.getRol() == Rol.ADMIN) {
            throw new BadRequestException(
                    "No se puede desactivar un usuario ADMIN",
                    "ADMIN_NO_DESACTIVABLE", "usuarioId");
        }

        if (!usuario.isActivo()) {
            throw new BadRequestException(
                    "El usuario ya está desactivado",
                    "USUARIO_YA_DESACTIVADO",
                    "usuarioId");
        }

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        realtimePublisher.usuarioActualizado(toDTO(usuario));
    }

    @Transactional
    public void reactivarUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> {
                    log.warn(
                            "Usuario no encontrado al reactivar. usuarioId='{}'",
                            usuarioId);
                    return new NotFoundException(
                            "Usuario no encontrado",
                            "USUARIO_NOT_FOUND");
                });

        if (usuario.isActivo()) {
            throw new BadRequestException(
                    "El usuario ya está activo",
                    "USUARIO_YA_ACTIVO", "usuarioId");
        }

        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        realtimePublisher.usuarioActualizado(toDTO(usuario));
    }

    @Transactional
    public void editarUsuario(Long id, EditarUsuarioRequest req) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "Usuario no encontrado al editar. id='{}'",
                            id);
                    return new NotFoundException(
                            "Usuario no encontrado",
                            "USUARIO_NOT_FOUND");
                });

        if (req.activo() != null && !req.activo() && usuario.getRol() == Rol.ADMIN) {
            throw new ForbiddenException(
                    "No se puede desactivar un usuario ADMIN",
                    "ADMIN_NO_DESACTIVABLE");
        }

        if (req.nombre() != null && !req.nombre().isBlank()) {
            usuario.setNombre(req.nombre().trim());
        }

        if (req.rol() != null) {
            try {
                usuario.setRol(Rol.valueOf(req.rol().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Rol inválido", "ROL_INVALIDO", "rol");
            }
        }

        if (req.activo() != null) {
            usuario.setActivo(req.activo());
        }

        usuarioRepository.save(usuario);
        realtimePublisher.usuarioActualizado(toDTO(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarUsuarios(String nombre, String rol, Boolean activo) {
        Rol rolEnum = null;
        if (rol != null && !rol.isBlank()) {
            try {
                rolEnum = Rol.valueOf(rol.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                        "Rol inválido",
                        "ROL_INVALIDO",
                        "rol");
            }
        }

        return usuarioRepository.findByFiltros(
                        (nombre == null || nombre.isBlank()) ? null : nombre.trim(),
                        rolEnum,
                        activo
                )
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtenerUsuarioPorId(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .map(this::toDTO)
                .orElseThrow(() -> {
                    log.warn(
                            "Usuario no encontrado. usuarioId='{}'",
                            usuarioId);
                    return new NotFoundException(
                            "Usuario no encontrado",
                            "USUARIO_NOT_FOUND");
                });
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

