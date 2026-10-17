package com.rutaexpress.catalog.messaging;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeadLetterListenerTest {

    @Mock
    private Channel channel;

    @Test
    void registraElMensajeMuertoYLoConfirmaConAck() throws Exception {
        MessageProperties propiedades = new MessageProperties();
        propiedades.setDeliveryTag(11L);
        propiedades.setHeader("x-first-death-queue", "q.envio.comandos");
        propiedades.setHeader("x-first-death-reason", "rejected");
        Message mensaje = new Message("{\"pesoKg\":-5}".getBytes(StandardCharsets.UTF_8), propiedades);

        new DeadLetterListener().onMensajeMuerto(mensaje, channel);

        verify(channel).basicAck(11L, false);
    }

    @Test
    void tambienConfirmaMensajesSinCabecerasDeMuerte() throws Exception {
        MessageProperties propiedades = new MessageProperties();
        propiedades.setDeliveryTag(12L);
        Message mensaje = new Message("basura".getBytes(StandardCharsets.UTF_8), propiedades);

        new DeadLetterListener().onMensajeMuerto(mensaje, channel);

        verify(channel).basicAck(12L, false);
    }
}
