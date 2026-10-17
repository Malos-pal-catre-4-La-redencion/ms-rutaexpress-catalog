package com.rutaexpress.catalog.exception;

import java.math.BigDecimal;

/** Error recuperable: puede que en unos segundos se libere un vehículo, por eso se reintenta. */
public class SinVehiculoDisponibleException extends RuntimeException {

    public SinVehiculoDisponibleException(BigDecimal pesoKg) {
        super("No hay un vehículo disponible con capacidad para " + pesoKg + " kg");
    }
}
