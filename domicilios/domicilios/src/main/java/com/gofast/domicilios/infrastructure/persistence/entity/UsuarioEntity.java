package com.gofast.domicilios.infrastructure.persistence.entity;

import com.gofast.domicilios.domain.model.EstadoDelivery;
import com.gofast.domicilios.domain.model.Rol;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDelivery estadoDelivery = EstadoDelivery.DESCONECTADO;

    private LocalDateTime disponibleDesde;

    @Column(name = "email_verificado", nullable = false)
    private boolean emailVerificado = false;

    @Column(name = "codigo_verificacion")
    private String codigoVerificacion;

    @Column(name = "codigo_expiracion")
    private LocalDateTime codigoExpiracion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public EstadoDelivery getEstadoDelivery() {
        return estadoDelivery;
    }

    public void setEstadoDelivery(EstadoDelivery estadoDelivery) {
        this.estadoDelivery = estadoDelivery;
    }

    public LocalDateTime getDisponibleDesde() {
        return disponibleDesde;
    }

    public void setDisponibleDesde(LocalDateTime disponibleDesde) {
        this.disponibleDesde = disponibleDesde;
    }

    public boolean isEmailVerificado() {
        return emailVerificado;
    }

    public void setEmailVerificado(boolean emailVerificado) {
        this.emailVerificado = emailVerificado;
    }

    public String getCodigoVerificacion() {
        return codigoVerificacion;
    }

    public void setCodigoVerificacion(String codigoVerificacion) {
        this.codigoVerificacion = codigoVerificacion;
    }

    public LocalDateTime getCodigoExpiracion() {
        return codigoExpiracion;
    }

    public void setCodigoExpiracion(LocalDateTime codigoExpiracion) {
        this.codigoExpiracion = codigoExpiracion;
    }
}
