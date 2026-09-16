package com.rutaexpress.catalog.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ServicioRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        String descripcion,
        @NotNull(message = "La tarifa base es obligatoria") @Positive(message = "La tarifa debe ser mayor a 0") BigDecimal tarifaBase,
        @NotNull(message = "El tiempo estimado es obligatorio") @Positive(message = "El tiempo estimado debe ser mayor a 0") Integer tiempoEstimadoHoras
) {
}
