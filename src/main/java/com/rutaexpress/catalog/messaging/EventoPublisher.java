package com.rutaexpress.catalog.messaging;

import com.rutaexpress.catalog.config.MensajeriaProperties;
import com.rutaexpress.catalog.messaging.mensajes.VehiculoAsignadoEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Productor: publica los eventos de catalog en el exchange topic. */
@Component
public class EventoPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventoPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final MensajeriaProperties props;

    public EventoPublisher(RabbitTemplate rabbitTemplate, MensajeriaProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    public void publicarVehiculoAsignado(VehiculoAsignadoEvento evento) {
        rabbitTemplate.convertAndSend(props.exchanges().eventos(), props.eventos().vehiculoAsignado(), evento);
        log.info("Evento publicado: {} -> {} ({})",
                props.exchanges().eventos(), props.eventos().vehiculoAsignado(), evento.trackingCode());
    }
}
