package com.gofast.domicilios.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class Pedido {
    private Long id;
    private Long clienteId;
    private Long direccionId;
    private Long domiciliarioId;
    private EstadoPedido estado;
    private BigDecimal total;
    private LocalDateTime fechaCreacion;

    public Pedido() {}

    public Pedido(Long id, LocalDateTime fechaCreacion, BigDecimal total, EstadoPedido estado, Long domiciliarioId, Long direccionId, Long clienteId) {
        this.id = id;
        this.fechaCreacion = fechaCreacion;
        this.total = total;
        this.estado = estado;
        this.domiciliarioId = domiciliarioId;
        this.direccionId = direccionId;
        this.clienteId = clienteId;
    }

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

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Long getDireccionId() {
        return direccionId;
    }

    public void setDireccionId(Long direccionId) {
        this.direccionId = direccionId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
}
