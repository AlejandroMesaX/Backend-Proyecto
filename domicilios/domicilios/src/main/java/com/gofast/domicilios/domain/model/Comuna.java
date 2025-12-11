package com.gofast.domicilios.domain.model;

import java.math.BigDecimal;

public class Comuna {
    private Long id;
    private Integer numero;
    private String nombre;
    private BigDecimal tarifaBase;
    private BigDecimal recargoPorSalto;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getTarifaBase() {
        return tarifaBase;
    }

    public void setTarifaBase(BigDecimal tarifaBase) {
        this.tarifaBase = tarifaBase;
    }

    public BigDecimal getRecargoPorSalto() {
        return recargoPorSalto;
    }

    public void setRecargoPorSalto(BigDecimal recargoPorSalto) {
        this.recargoPorSalto = recargoPorSalto;
    }
}
