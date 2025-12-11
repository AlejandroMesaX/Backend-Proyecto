package com.gofast.domicilios.domain.service;

import com.gofast.domicilios.application.exception.BadRequestException;
import com.gofast.domicilios.domain.model.Barrio;
import com.gofast.domicilios.domain.model.Comuna;
import com.gofast.domicilios.domain.repository.BarrioRepositoryPort;
import com.gofast.domicilios.domain.repository.ComunaRepositoryPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TarifaDomicilioService {
    private final BarrioRepositoryPort barrioRepository;
    private final ComunaRepositoryPort comunaRepository;

    public TarifaDomicilioService(BarrioRepositoryPort barrioRepository,
                                  ComunaRepositoryPort comunaRepository) {
        this.barrioRepository = barrioRepository;
        this.comunaRepository = comunaRepository;
    }

    public BigDecimal calcularCosto(String barrioRecogidaNombre, String barrioEntregaNombre) {
        // 1. Encontrar barrios
        Barrio origen = barrioRepository.findByNombre(barrioRecogidaNombre)
                .orElseThrow(() -> new BadRequestException("Barrio de recogida no encontrado: " + barrioRecogidaNombre));

        Barrio destino = barrioRepository.findByNombre(barrioEntregaNombre)
                .orElseThrow(() -> new BadRequestException("Barrio de entrega no encontrado: " + barrioEntregaNombre));

        if (origen.getComuna() == null || destino.getComuna() == null) {
            throw new BadRequestException("Los barrios deben tener comuna asociada");
        }

        // 2. Buscar comunas
        Comuna comunaOrigen = comunaRepository.findByNumero(origen.getComuna())
                .orElseThrow(() -> new BadRequestException("Comuna de origen no encontrada: " + origen.getComuna()));

        Comuna comunaDestino = comunaRepository.findByNumero(destino.getComuna())
                .orElseThrow(() -> new BadRequestException("Comuna de destino no encontrada: " + destino.getComuna()));

        int diferencia = Math.abs(comunaOrigen.getNumero() - comunaDestino.getNumero());

        // 3. Regla de negocio: base = max(tarifas de ambas comunas)
        BigDecimal base = comunaOrigen.getTarifaBase().max(comunaDestino.getTarifaBase());

        // 4. Recargo: usamos el mayor recargo de las dos comunas
        BigDecimal recargoPorSalto = comunaOrigen.getRecargoPorSalto().max(comunaDestino.getRecargoPorSalto());
        BigDecimal recargo = recargoPorSalto.multiply(BigDecimal.valueOf(diferencia));

        return base.add(recargo);
    }
}
