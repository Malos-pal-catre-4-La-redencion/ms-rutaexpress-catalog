package com.rutaexpress.catalog.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "vehiculos")
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String patente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoVehiculo tipo;

    @Column(name = "capacidad_kg", nullable = false)
    private BigDecimal capacidadKg;

    @Column(nullable = false)
    private boolean disponible;

    protected Vehiculo() {
        // JPA
    }

    public Vehiculo(String patente, TipoVehiculo tipo, BigDecimal capacidadKg) {
        this.patente = patente;
        this.tipo = tipo;
        this.capacidadKg = capacidadKg;
        this.disponible = true;
    }

    public void cambiarDisponibilidad(boolean disponible) {
        this.disponible = disponible;
    }

    public Long getId() {
        return id;
    }

    public String getPatente() {
        return patente;
    }

    public TipoVehiculo getTipo() {
        return tipo;
    }

    public BigDecimal getCapacidadKg() {
        return capacidadKg;
    }

    public boolean isDisponible() {
        return disponible;
    }
}
