package com.rutaexpress.catalog.messaging;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Consume la DLQ de comandos y deja registro en el log de cada mensaje no
 * entregado: cola de origen, motivo (rejected, expired o maxlen) y contenido.
 */
@Component
public class DeadLetterListener {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterListener.class);

    @RabbitListener(queues = "${rutaexpress.mensajeria.comandos.dlq}")
    public void onMensajeMuerto(Message message, Channel channel) throws IOException {
        MessageProperties propiedades = message.getMessageProperties();

        log.error("[DLQ] Mensaje no entregado. colaOrigen={} motivo={} contenido={}",
                propiedades.getHeaders().get("x-first-death-queue"),
                propiedades.getHeaders().get("x-first-death-reason"),
                new String(message.getBody(), StandardCharsets.UTF_8));

        channel.basicAck(propiedades.getDeliveryTag(), false);
    }
}
