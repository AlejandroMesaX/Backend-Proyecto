package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.dto.EditarUsuarioRequest;
import com.gofast.domicilios.application.dto.RegisterUsuarioRequest;
import com.gofast.domicilios.application.dto.UsuarioDTO;
import com.gofast.domicilios.application.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.gofast.domicilios.infrastructure.security.CustomUserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Boolean activo
    ) {
        return ResponseEntity.ok(usuarioService.listarUsuarios(nombre, rol, activo));
    }


    @PostMapping
    public ResponseEntity<UsuarioDTO> crearUsuario(@RequestBody RegisterUsuarioRequest req) {
        UsuarioDTO creado = usuarioService.registrarUsuario(req);
        return ResponseEntity.ok(creado);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> editarUsuario(
            @PathVariable Long id,
            @RequestBody EditarUsuarioRequest req
    ) {
        usuarioService.editarUsuario(id, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> eliminarUsuario(
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        usuarioService.desactivarUsuario(usuarioId, currentUser.getId());
        return ResponseEntity.noContent().build(); // 204
    }

    @PatchMapping("/{usuarioId}/reactivar")
    public ResponseEntity<Void> reactivarUsuario(@PathVariable Long usuarioId) {
        usuarioService.reactivarUsuario(usuarioId);
        return ResponseEntity.noContent().build(); // 204
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<UsuarioDTO> obtenerUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(usuarioService.obtenerUsuarioPorId(usuarioId));
    }
}
