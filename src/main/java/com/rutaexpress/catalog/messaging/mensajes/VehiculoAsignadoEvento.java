package com.rutaexpress.catalog.messaging.mensajes;

import java.time.Instant;

/** Evento que catalog publica cuando reservó un vehículo para un envío. */
public record VehiculoAsignadoEvento(Long envioId, String trackingCode, Long vehiculoId,
                                     String patente, Instant ocurridoEn) {
}
