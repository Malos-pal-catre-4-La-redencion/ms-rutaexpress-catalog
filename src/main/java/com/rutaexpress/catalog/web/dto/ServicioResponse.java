package com.rutaexpress.catalog.web.dto;

import com.rutaexpress.catalog.model.ServicioEnvio;
import java.math.BigDecimal;

public record ServicioResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal tarifaBase,
        Integer tiempoEstimadoHoras,
        boolean activo
) {
    public static ServicioResponse desde(ServicioEnvio s) {
        return new ServicioResponse(s.getId(), s.getNombre(), s.getDescripcion(), s.getTarifaBase(), s.getTiempoEstimadoHoras(), s.isActivo());
    }
}
