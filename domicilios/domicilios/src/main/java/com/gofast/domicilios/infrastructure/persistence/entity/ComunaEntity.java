package com.gofast.domicilios.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "comunas")
public class ComunaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false, unique = true)
    private Integer numero;

    @Column(name = "tarifa_base", nullable = false)
    private BigDecimal tarifaBase;

    @Column(name = "recargo_por_salto", nullable = false)
    private BigDecimal recargoPorSalto;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getTarifaBase() {
        return tarifaBase;
    }

    public void setTarifaBase(BigDecimal tarifaBase) {
        this.tarifaBase = tarifaBase;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public BigDecimal getRecargoPorSalto() {
        return recargoPorSalto;
    }

    public void setRecargoPorSalto(BigDecimal recargoPorSalto) {
        this.recargoPorSalto = recargoPorSalto;
    }
}
