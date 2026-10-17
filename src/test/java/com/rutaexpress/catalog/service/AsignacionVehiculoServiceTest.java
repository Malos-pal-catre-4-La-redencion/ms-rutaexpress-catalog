package com.rutaexpress.catalog.service;

import com.rutaexpress.catalog.exception.ComandoInvalidoException;
import com.rutaexpress.catalog.exception.SinVehiculoDisponibleException;
import com.rutaexpress.catalog.messaging.mensajes.AsignarVehiculoComando;
import com.rutaexpress.catalog.model.TipoVehiculo;
import com.rutaexpress.catalog.model.Vehiculo;
import com.rutaexpress.catalog.repository.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsignacionVehiculoServiceTest {

    @Mock
    private VehiculoRepository repository;

    private AsignacionVehiculoService service;

    @BeforeEach
    void setUp() {
        service = new AsignacionVehiculoService(repository);
    }

    @Test
    void reservaElVehiculoDisponibleConCapacidad() {
        Vehiculo furgon = new Vehiculo("ABCD-12", TipoVehiculo.FURGON, new BigDecimal("800"));
        when(repository.findFirstByDisponibleTrueAndCapacidadKgGreaterThanEqualOrderByCapacidadKgAsc(new BigDecimal("300")))
                .thenReturn(Optional.of(furgon));

        Vehiculo asignado = service.asignarParaEnvio(
                new AsignarVehiculoComando(1L, "RE-AAAA1111", new BigDecimal("300")));

        assertSame(furgon, asignado);
        assertFalse(asignado.isDisponible());
        verify(repository).save(furgon);
    }

    @Test
    void lanzaErrorRecuperableSiNoHayVehiculoConCapacidad() {
        when(repository.findFirstByDisponibleTrueAndCapacidadKgGreaterThanEqualOrderByCapacidadKgAsc(new BigDecimal("99999")))
                .thenReturn(Optional.empty());

        assertThrows(SinVehiculoDisponibleException.class, () -> service.asignarParaEnvio(
                new AsignarVehiculoComando(1L, "RE-AAAA1111", new BigDecimal("99999"))));

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rechazaPesoCeroONegativoSinConsultarLaBaseDeDatos() {
        assertThrows(ComandoInvalidoException.class, () -> service.asignarParaEnvio(
                new AsignarVehiculoComando(1L, "RE-AAAA1111", BigDecimal.ZERO)));
        assertThrows(ComandoInvalidoException.class, () -> service.asignarParaEnvio(
                new AsignarVehiculoComando(1L, "RE-AAAA1111", new BigDecimal("-5"))));

        verifyNoInteractions(repository);
    }

    @Test
    void rechazaComandosIncompletosSinConsultarLaBaseDeDatos() {
        assertThrows(ComandoInvalidoException.class, () -> service.asignarParaEnvio(null));
        assertThrows(ComandoInvalidoException.class, () -> service.asignarParaEnvio(
                new AsignarVehiculoComando(null, "RE-AAAA1111", BigDecimal.TEN)));
        assertThrows(ComandoInvalidoException.class, () -> service.asignarParaEnvio(
                new AsignarVehiculoComando(1L, "  ", BigDecimal.TEN)));
        assertThrows(ComandoInvalidoException.class, () -> service.asignarParaEnvio(
                new AsignarVehiculoComando(1L, "RE-AAAA1111", null)));

        verifyNoInteractions(repository);
    }
}
