package com.gofast.domicilios.infrastructure.persistence.entity;

import com.gofast.domicilios.domain.model.EstadoPedido;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "domiciliario_id")
    private Long domiciliarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    @Column(name = "costo_servicio", nullable = false)
    private BigDecimal costoServicio;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "direccion_recogida", nullable = false)
    private String direccionRecogida;

    @Column(name = "barrio_recogida", nullable = false)
    private String barrioRecogida;

    @Column(name = "telefono_contacto_recogida", nullable = false)
    private String telefonoContactoRecogida;

    @Column(name = "direccion_entrega", nullable = false)
    private String direccionEntrega;

    @Column(name = "barrio_entrega", nullable = false)
    private String barrioEntrega;

    @Column(name = "nombre_quien_recibe", nullable = false)
    private String nombreQuienRecibe;

    @Column(name = "telefono_quien_recibe", nullable = false)
    private String telefonoQuienRecibe;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
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
}
