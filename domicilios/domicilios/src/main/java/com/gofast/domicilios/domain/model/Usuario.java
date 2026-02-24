package com.gofast.domicilios.domain.model;

import java.time.LocalDateTime;

public class Usuario {
    private Long id;
    private String nombre;
    private String email;
    private String passwordHash;
    private Rol rol;
    private boolean activo;
    private EstadoDelivery estadoDelivery;
    private LocalDateTime disponibleDesde;

    public Usuario() {}

    public Usuario(Long id, String nombre, String email, String passwordHash, Rol rol, boolean activo){
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String password) {
        this.passwordHash = password;
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
}
