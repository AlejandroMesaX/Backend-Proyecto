package com.gofast.domicilios.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class Pedido {
    private Long id;
    private Long clienteId;
    private Long domiciliarioId;
    private EstadoPedido estado;
    private BigDecimal costoServicio;
    private LocalDateTime fechaCreacion;

    private String direccionRecogida;
    private String barrioRecogida;
    private String telefonoContactoRecogida;

    private String direccionEntrega;
    private String barrioEntrega;
    private String nombreQuienRecibe;
    private String telefonoQuienRecibe;

    private String motivoIncidencia;
    private LocalDateTime fechaIncidencia;

    public Pedido() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDomiciliarioId() {
        return domiciliarioId;
    }

    public void setDomiciliarioId(Long domiciliarioId) {
        this.domiciliarioId = domiciliarioId;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public BigDecimal getCostoServicio() {
        return costoServicio;
    }

    public void setCostoServicio(BigDecimal costoServicio) {
        this.costoServicio = costoServicio;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getDireccionRecogida() {
        return direccionRecogida;
    }

    public void setDireccionRecogida(String direccionRecogida) {
        this.direccionRecogida = direccionRecogida;
    }

    public String getBarrioRecogida() {
        return barrioRecogida;
    }

    public void setBarrioRecogida(String barrioRecogida) {
        this.barrioRecogida = barrioRecogida;
    }

    public String getTelefonoContactoRecogida() {
        return telefonoContactoRecogida;
    }

    public void setTelefonoContactoRecogida(String telefonoContactoRecogida) {
        this.telefonoContactoRecogida = telefonoContactoRecogida;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public String getBarrioEntrega() {
        return barrioEntrega;
    }

    public void setBarrioEntrega(String barrioEntrega) {
        this.barrioEntrega = barrioEntrega;
    }

    public String getNombreQuienRecibe() {
        return nombreQuienRecibe;
    }

    public void setNombreQuienRecibe(String nombreQuienRecibe) {
        this.nombreQuienRecibe = nombreQuienRecibe;
    }

    public String getTelefonoQuienRecibe() {
        return telefonoQuienRecibe;
    }

    public void setTelefonoQuienRecibe(String telefonoQuienRecibe) {
        this.telefonoQuienRecibe = telefonoQuienRecibe;
    }

    public String getMotivoIncidencia() {
        return motivoIncidencia;
    }

    public void setMotivoIncidencia(String motivoIncidencia) {
        this.motivoIncidencia = motivoIncidencia;
    }

    public LocalDateTime getFechaIncidencia() {
        return fechaIncidencia;
    }

    public void setFechaIncidencia(LocalDateTime fechaIncidencia) {
        this.fechaIncidencia = fechaIncidencia;
    }
}
