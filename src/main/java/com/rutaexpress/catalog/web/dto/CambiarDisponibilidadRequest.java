package com.rutaexpress.catalog.web.dto;

import jakarta.validation.constraints.NotNull;

public record CambiarDisponibilidadRequest(
        @NotNull(message = "El campo disponible es obligatorio") Boolean disponible
) {
}
