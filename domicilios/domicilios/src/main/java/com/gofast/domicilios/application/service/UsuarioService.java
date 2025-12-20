package com.gofast.domicilios.application.service;

import com.gofast.domicilios.application.dto.RegisterUsuarioRequest;
import com.gofast.domicilios.application.dto.UsuarioDTO;
import com.gofast.domicilios.domain.model.Rol;
import com.gofast.domicilios.domain.model.Usuario;
import com.gofast.domicilios.application.exception.NotFoundException;
import com.gofast.domicilios.domain.repository.UsuarioRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public void desactivarUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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

