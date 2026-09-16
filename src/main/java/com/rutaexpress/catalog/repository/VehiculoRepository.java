package com.rutaexpress.catalog.repository;

import com.rutaexpress.catalog.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
}
