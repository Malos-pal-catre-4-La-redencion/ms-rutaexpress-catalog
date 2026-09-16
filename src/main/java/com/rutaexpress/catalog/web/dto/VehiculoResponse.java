package com.rutaexpress.catalog.web.dto;

import com.rutaexpress.catalog.model.TipoVehiculo;
import com.rutaexpress.catalog.model.Vehiculo;
import java.math.BigDecimal;

public record VehiculoResponse(
        Long id,
        String patente,
        TipoVehiculo tipo,
        BigDecimal capacidadKg,
        boolean disponible
) {
    public static VehiculoResponse desde(Vehiculo v) {
        return new VehiculoResponse(v.getId(), v.getPatente(), v.getTipo(), v.getCapacidadKg(), v.isDisponible());
    }
}
