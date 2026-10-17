package com.rutaexpress.catalog.service;

import com.rutaexpress.catalog.exception.ComandoInvalidoException;
import com.rutaexpress.catalog.exception.SinVehiculoDisponibleException;
import com.rutaexpress.catalog.messaging.mensajes.AsignarVehiculoComando;
import com.rutaexpress.catalog.model.Vehiculo;
import com.rutaexpress.catalog.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Regla de negocio de la asignación: reservar un vehículo con capacidad para
 * el envío. No sabe nada de RabbitMQ: el listener la invoca y decide el ACK/NACK.
 */
@Service
public class AsignacionVehiculoService {

    private final VehiculoRepository repository;

    public AsignacionVehiculoService(VehiculoRepository repository) {
        this.repository = repository;
    }

    /**
     * @throws ComandoInvalidoException      comando incompleto o con peso inválido (no recuperable)
     * @throws SinVehiculoDisponibleException no hay vehículo con capacidad (recuperable)
     */
    @Transactional
    public Vehiculo asignarParaEnvio(AsignarVehiculoComando comando) {
        validar(comando);

        Vehiculo vehiculo = repository
                .findFirstByDisponibleTrueAndCapacidadKgGreaterThanEqualOrderByCapacidadKgAsc(comando.pesoKg())
                .orElseThrow(() -> new SinVehiculoDisponibleException(comando.pesoKg()));

        vehiculo.cambiarDisponibilidad(false);
        repository.save(vehiculo);
        return vehiculo;
    }

    private void validar(AsignarVehiculoComando comando) {
        if (comando == null) {
            throw new ComandoInvalidoException("El comando está vacío");
        }
        if (comando.envioId() == null) {
            throw new ComandoInvalidoException("El comando no trae envioId");
        }
        if (comando.trackingCode() == null || comando.trackingCode().isBlank()) {
            throw new ComandoInvalidoException("El comando no trae trackingCode");
        }
        if (comando.pesoKg() == null || comando.pesoKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ComandoInvalidoException("El peso debe ser mayor que 0 (llegó: " + comando.pesoKg() + ")");
        }
    }
}
