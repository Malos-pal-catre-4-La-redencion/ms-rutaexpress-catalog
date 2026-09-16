package com.rutaexpress.catalog.web.dto;

import com.rutaexpress.catalog.model.TipoVehiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record VehiculoRequest(
        @NotBlank(message = "La patente es obligatoria") String patente,
        @NotNull(message = "El tipo de vehículo es obligatorio") TipoVehiculo tipo,
        @NotNull(message = "La capacidad es obligatoria") @Positive(message = "La capacidad debe ser mayor a 0") BigDecimal capacidadKg
) {
}
