package com.rutaexpress.catalog.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rutaexpress.catalog.config.MensajeriaProperties;
import com.rutaexpress.catalog.exception.ComandoInvalidoException;
import com.rutaexpress.catalog.messaging.mensajes.AsignarVehiculoComando;
import com.rutaexpress.catalog.messaging.mensajes.VehiculoAsignadoEvento;
import com.rutaexpress.catalog.model.Vehiculo;
import com.rutaexpress.catalog.service.AsignacionVehiculoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Consumidor de los comandos de envío. Solo orquesta: decodifica el mensaje,
 * llama al servicio de negocio y decide entre ACK y NACK.
 *
 * - Mensaje ilegible o comando inválido (no recuperable): NACK sin requeue -> DLQ.
 * - Error recuperable (p. ej. sin vehículo disponible): reintentos acotados; si
 *   persiste, NACK sin requeue -> DLQ.
 * - Éxito: ACK y luego se publica el evento con el resultado.
 */
@Component
public class ComandoEnvioListener {

    private static final Logger log = LoggerFactory.getLogger(ComandoEnvioListener.class);

    private final AsignacionVehiculoService asignacionService;
    private final EventoPublisher eventoPublisher;
    private final ObjectMapper objectMapper;
    private final MensajeriaProperties props;

    public ComandoEnvioListener(AsignacionVehiculoService asignacionService,
                                EventoPublisher eventoPublisher,
                                ObjectMapper objectMapper,
                                MensajeriaProperties props) {
        this.asignacionService = asignacionService;
        this.eventoPublisher = eventoPublisher;
        this.objectMapper = objectMapper;
        this.props = props;
    }

    @RabbitListener(queues = "${rutaexpress.mensajeria.comandos.cola}")
    public void onAsignarVehiculo(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();

        AsignarVehiculoComando comando;
        try {
            comando = objectMapper.readValue(message.getBody(), AsignarVehiculoComando.class);
        } catch (IOException ex) {
            log.error("Comando ilegible, se envía a la DLQ: {}", ex.getMessage());
            channel.basicNack(tag, false, false);
            return;
        }

        Vehiculo vehiculo;
        try {
            vehiculo = asignarConReintentos(comando);
        } catch (ComandoInvalidoException ex) {
            log.error("Comando inválido (no recuperable), se envía a la DLQ: {}", ex.getMessage());
            channel.basicNack(tag, false, false);
            return;
        } catch (RuntimeException ex) {
            log.error("Se agotaron los reintentos para el envío {}, se envía a la DLQ: {}",
                    comando.trackingCode(), ex.getMessage());
            channel.basicNack(tag, false, false);
            return;
        }

        channel.basicAck(tag, false);
        log.info("Vehículo {} asignado al envío {}", vehiculo.getPatente(), comando.trackingCode());

        try {
            eventoPublisher.publicarVehiculoAsignado(new VehiculoAsignadoEvento(
                    comando.envioId(), comando.trackingCode(), vehiculo.getId(),
                    vehiculo.getPatente(), Instant.now()));
        } catch (RuntimeException ex) {
            // El comando ya se confirmó: se registra el fallo en lugar de reasignar otro vehículo.
            log.error("El vehículo se asignó pero no se pudo publicar el evento del envío {}: {}",
                    comando.trackingCode(), ex.getMessage());
        }
    }

    /** Reintenta solo los errores recuperables; un comando inválido se propaga de inmediato. */
    private Vehiculo asignarConReintentos(AsignarVehiculoComando comando) {
        int maxIntentos = Math.max(1, props.reintentos().maxIntentos());
        RuntimeException ultimoError = null;

        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                return asignacionService.asignarParaEnvio(comando);
            } catch (ComandoInvalidoException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                ultimoError = ex;
                log.warn("Intento {}/{} fallido para el comando {}: {}",
                        intento, maxIntentos, comando, ex.getMessage());
                if (intento < maxIntentos && !esperar(props.reintentos().esperaMs() * intento)) {
                    break;
                }
            }
        }
        throw ultimoError;
    }

    /** @return false si el hilo fue interrumpido y no conviene seguir reintentando. */
    private boolean esperar(long milisegundos) {
        if (milisegundos <= 0) {
            return true;
        }
        try {
            Thread.sleep(milisegundos);
            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
