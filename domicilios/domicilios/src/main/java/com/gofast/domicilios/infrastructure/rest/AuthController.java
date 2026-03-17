package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.dto.*;
import com.gofast.domicilios.application.service.DeliveryService;
import com.gofast.domicilios.application.service.UsuarioService;
import com.gofast.domicilios.domain.model.Rol;
import com.gofast.domicilios.domain.model.Usuario;
import com.gofast.domicilios.infrastructure.security.CustomUserDetails;
import com.gofast.domicilios.infrastructure.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final DeliveryService deliveryService;

    public AuthController(UsuarioService usuarioService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          DeliveryService deliveryService) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.deliveryService = deliveryService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> register(@RequestBody RegisterUsuarioRequest req) {
        UsuarioDTO usuario = usuarioService.registrarUsuario(req);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {

        // Verificar PRIMERO antes de autenticar
        Usuario usuario = usuarioService.obtenerEntidadPorEmail(req.email);
        if (!usuario.isEmailVerificado()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Debes verificar tu correo antes de iniciar sesión.");
        }

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email, req.password)
        );

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        if (userDetails.getRol() == Rol.DELIVERY) {
            deliveryService.setDisponible(req.email, false);
        }

        LoginResponse resp = new LoginResponse();
        resp.token = token;
        resp.usuario = usuarioService.obtenerPorEmail(req.email);

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/verify")
    public ResponseEntity<LoginResponse> verify(@RequestBody VerificacionRequest req) {
        Usuario usuario = usuarioService.verificarYActivar(req.email, req.code);

        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        String token = jwtTokenProvider.generateToken(userDetails);

        LoginResponse resp = new LoginResponse();
        resp.token = token;
        resp.usuario = usuarioService.toDTO(usuario);

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/resend-code")
    public ResponseEntity<Void> resendCode(@RequestBody ResendCodeRequest req) {
        usuarioService.reenviarCodigo(req.email);
        return ResponseEntity.ok().build();
    }
}
