package com.rutaexpress.catalog.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "servicios_envio")
public class ServicioEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column
    private String descripcion;

    @Column(name = "tarifa_base", nullable = false)
    private BigDecimal tarifaBase;

    @Column(name = "tiempo_estimado_horas", nullable = false)
    private Integer tiempoEstimadoHoras;

    @Column(nullable = false)
    private boolean activo;

    protected ServicioEnvio() {
        // JPA
    }

    public ServicioEnvio(String nombre, String descripcion, BigDecimal tarifaBase, Integer tiempoEstimadoHoras) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tarifaBase = tarifaBase;
        this.tiempoEstimadoHoras = tiempoEstimadoHoras;
        this.activo = true;
    }

    public void actualizar(String nombre, String descripcion, BigDecimal tarifaBase, Integer tiempoEstimadoHoras) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tarifaBase = tarifaBase;
        this.tiempoEstimadoHoras = tiempoEstimadoHoras;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void activar() {
        this.activo = true;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getTarifaBase() {
        return tarifaBase;
    }

    public Integer getTiempoEstimadoHoras() {
        return tiempoEstimadoHoras;
    }

    public boolean isActivo() {
        return activo;
    }
}
