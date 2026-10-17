package com.rutaexpress.catalog.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rutaexpress.catalog.exception.ComandoInvalidoException;
import com.rutaexpress.catalog.exception.SinVehiculoDisponibleException;
import com.rutaexpress.catalog.messaging.mensajes.AsignarVehiculoComando;
import com.rutaexpress.catalog.messaging.mensajes.VehiculoAsignadoEvento;
import com.rutaexpress.catalog.model.TipoVehiculo;
import com.rutaexpress.catalog.model.Vehiculo;
import com.rutaexpress.catalog.service.AsignacionVehiculoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComandoEnvioListenerTest {

    private static final long TAG = 7L;
    private static final String COMANDO_VALIDO =
            "{\"envioId\":1,\"trackingCode\":\"RE-AAAA1111\",\"pesoKg\":300}";

    @Mock
    private AsignacionVehiculoService asignacionService;

    @Mock
    private EventoPublisher eventoPublisher;

    @Mock
    private Channel channel;

    private ComandoEnvioListener listener;

    @BeforeEach
    void setUp() {
        listener = new ComandoEnvioListener(asignacionService, eventoPublisher,
                new ObjectMapper(), PropiedadesDePrueba.crear());
    }

    private Message mensaje(String cuerpo) {
        MessageProperties propiedades = new MessageProperties();
        propiedades.setDeliveryTag(TAG);
        return new Message(cuerpo.getBytes(StandardCharsets.UTF_8), propiedades);
    }

    private Vehiculo furgon() {
        return new Vehiculo("ABCD-12", TipoVehiculo.FURGON, new BigDecimal("800"));
    }

    @Test
    void comandoValidoSeConfirmaConAckYPublicaElEvento() throws Exception {
        when(asignacionService.asignarParaEnvio(any(AsignarVehiculoComando.class))).thenReturn(furgon());

        listener.onAsignarVehiculo(mensaje(COMANDO_VALIDO), channel);

        verify(channel).basicAck(TAG, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());

        ArgumentCaptor<VehiculoAsignadoEvento> evento = ArgumentCaptor.forClass(VehiculoAsignadoEvento.class);
        verify(eventoPublisher).publicarVehiculoAsignado(evento.capture());
        assertEquals("RE-AAAA1111", evento.getValue().trackingCode());
        assertEquals("ABCD-12", evento.getValue().patente());
    }

    @Test
    void mensajeQueNoEsJsonVaALaDlqSinReintentos() throws Exception {
        listener.onAsignarVehiculo(mensaje("esto no es json"), channel);

        verify(channel).basicNack(TAG, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verifyNoInteractions(asignacionService, eventoPublisher);
    }

    @Test
    void errorNoRecuperableVaALaDlqSinReintentar() throws Exception {
        when(asignacionService.asignarParaEnvio(any(AsignarVehiculoComando.class)))
                .thenThrow(new ComandoInvalidoException("peso inválido"));

        listener.onAsignarVehiculo(mensaje(COMANDO_VALIDO), channel);

        verify(asignacionService, times(1)).asignarParaEnvio(any(AsignarVehiculoComando.class));
        verify(channel).basicNack(TAG, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verifyNoInteractions(eventoPublisher);
    }

    @Test
    void errorRecuperablePersistenteSeReintentaYTerminaEnLaDlq() throws Exception {
        when(asignacionService.asignarParaEnvio(any(AsignarVehiculoComando.class)))
                .thenThrow(new SinVehiculoDisponibleException(new BigDecimal("300")));

        listener.onAsignarVehiculo(mensaje(COMANDO_VALIDO), channel);

        verify(asignacionService, times(3)).asignarParaEnvio(any(AsignarVehiculoComando.class));
        verify(channel).basicNack(TAG, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verifyNoInteractions(eventoPublisher);
    }

    @Test
    void errorRecuperableQueSeResuelveEnElSegundoIntentoSeConfirmaConAck() throws Exception {
        when(asignacionService.asignarParaEnvio(any(AsignarVehiculoComando.class)))
                .thenThrow(new SinVehiculoDisponibleException(new BigDecimal("300")))
                .thenReturn(furgon());

        listener.onAsignarVehiculo(mensaje(COMANDO_VALIDO), channel);

        verify(asignacionService, times(2)).asignarParaEnvio(any(AsignarVehiculoComando.class));
        verify(channel).basicAck(TAG, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void siFallaLaPublicacionDelEventoElComandoYaConfirmadoNoSeRechaza() throws Exception {
        when(asignacionService.asignarParaEnvio(any(AsignarVehiculoComando.class))).thenReturn(furgon());
        doThrow(new RuntimeException("broker caído"))
                .when(eventoPublisher).publicarVehiculoAsignado(any(VehiculoAsignadoEvento.class));

        listener.onAsignarVehiculo(mensaje(COMANDO_VALIDO), channel);

        verify(channel).basicAck(TAG, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }
}
