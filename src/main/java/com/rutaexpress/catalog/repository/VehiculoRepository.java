package com.rutaexpress.catalog.repository;

import com.rutaexpress.catalog.model.Vehiculo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.math.BigDecimal;
import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    /**
     * El vehículo disponible más pequeño que aún soporta el peso. El bloqueo
     * evita que dos consumidores reserven el mismo vehículo a la vez.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Vehiculo> findFirstByDisponibleTrueAndCapacidadKgGreaterThanEqualOrderByCapacidadKgAsc(BigDecimal pesoKg);
}
