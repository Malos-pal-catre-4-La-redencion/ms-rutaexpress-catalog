package com.rutaexpress.catalog.config;

import com.rutaexpress.catalog.messaging.PropiedadesDePrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RabbitTopologyConfigTest {

    private RabbitTopologyConfig config;

    @BeforeEach
    void setUp() {
        config = new RabbitTopologyConfig(PropiedadesDePrueba.crear());
    }

    @Test
    void losExchangesTienenElTipoEsperado() {
        assertEquals("direct", config.comandosExchange().getType());
        assertEquals("topic", config.eventosExchange().getType());
        assertEquals("direct", config.deadLetterExchange().getType());
    }

    @Test
    void laColaDeComandosTieneRetencionYApuntaALaDlq() {
        Queue cola = config.comandosQueue();

        assertEquals("q.envio.comandos", cola.getName());
        assertTrue(cola.isDurable());
        assertEquals(300000, cola.getArguments().get("x-message-ttl"));
        assertEquals(10000, cola.getArguments().get("x-max-length"));
        assertEquals("drop-head", cola.getArguments().get("x-overflow"));
        assertEquals("rutaexpress.dlx", cola.getArguments().get("x-dead-letter-exchange"));
        assertEquals("envio.comandos.dlq", cola.getArguments().get("x-dead-letter-routing-key"));
    }

    @Test
    void laDlqTieneSusPropiosLimitesYNoTieneOtraDlq() {
        Queue dlq = config.comandosDlq();

        assertEquals("q.envio.comandos.dlq", dlq.getName());
        assertEquals(604800000, dlq.getArguments().get("x-message-ttl"));
        assertEquals(10000, dlq.getArguments().get("x-max-length"));
        assertNull(dlq.getArguments().get("x-dead-letter-exchange"));
    }

    @Test
    void losBindingsEnlazanCadaColaConSuExchangeYRoutingKey() {
        Binding comandos = config.comandosBinding();
        assertEquals("rutaexpress.commands", comandos.getExchange());
        assertEquals("q.envio.comandos", comandos.getDestination());
        assertEquals("envio.asignar-vehiculo", comandos.getRoutingKey());

        Binding dlq = config.comandosDlqBinding();
        assertEquals("rutaexpress.dlx", dlq.getExchange());
        assertEquals("q.envio.comandos.dlq", dlq.getDestination());
        assertEquals("envio.comandos.dlq", dlq.getRoutingKey());
        assertFalse(dlq.getExchange().isBlank());
    }
}
