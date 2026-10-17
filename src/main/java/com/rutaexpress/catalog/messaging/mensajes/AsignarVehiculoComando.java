package com.rutaexpress.catalog.messaging.mensajes;

import java.math.BigDecimal;

/** Comando que shipments publica para pedir un vehículo con capacidad para el envío. */
public record AsignarVehiculoComando(Long envioId, String trackingCode, BigDecimal pesoKg) {
}
