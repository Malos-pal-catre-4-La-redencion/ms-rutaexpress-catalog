package com.rutaexpress.catalog.messaging;

import com.rutaexpress.catalog.config.MensajeriaProperties;

/** Mismos valores que application.yml, con reintentos sin espera para que las pruebas sean rápidas. */
public final class PropiedadesDePrueba {

    private PropiedadesDePrueba() {
    }

    public static MensajeriaProperties crear() {
        return new MensajeriaProperties(
                new MensajeriaProperties.Exchanges("rutaexpress.commands", "rutaexpress.events", "rutaexpress.dlx"),
                new MensajeriaProperties.Comandos("q.envio.comandos", "envio.asignar-vehiculo",
                        "q.envio.comandos.dlq", "envio.comandos.dlq", 300000, 10000),
                new MensajeriaProperties.Eventos("envio.vehiculo.asignado"),
                new MensajeriaProperties.Reintentos(3, 0),
                new MensajeriaProperties.Dlq(604800000, 10000));
    }
}
