package com.gofast.domicilios.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "barrios")
public class BarrioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comuna_id", nullable = false)
    private ComunaEntity comuna;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

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

    public ComunaEntity getComuna() {
        return comuna;
    }

    public void setComuna(ComunaEntity comuna) {
        this.comuna = comuna;
    }

    public boolean isActivo() {
        return activo; }

    public void setActivo(boolean activo) {
        this.activo = activo; }

}
