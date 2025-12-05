package com.gofast.domicilios.infrastructure.rest;

import com.gofast.domicilios.application.dto.*;
import com.gofast.domicilios.application.service.UsuarioService;
import com.gofast.domicilios.infrastructure.security.CustomUserDetails;
import com.gofast.domicilios.infrastructure.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UsuarioService usuarioService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> register(@RequestBody RegisterUsuarioRequest req) {
        UsuarioDTO usuario = usuarioService.registrarUsuario(req);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email, req.password)
        );

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        LoginResponse resp = new LoginResponse();
        resp.token = token;
        resp.usuario = usuarioService.obtenerPorEmail(req.email);

        return ResponseEntity.ok(resp);
    }
}
