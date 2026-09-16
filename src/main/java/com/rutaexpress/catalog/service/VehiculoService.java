package com.rutaexpress.catalog.service;

import com.rutaexpress.catalog.exception.RecursoNoEncontradoException;
import com.rutaexpress.catalog.model.Vehiculo;
import com.rutaexpress.catalog.repository.VehiculoRepository;
import com.rutaexpress.catalog.web.dto.VehiculoRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehiculoService {

    private final VehiculoRepository repository;

    public VehiculoService(VehiculoRepository repository) {
        this.repository = repository;
    }

    public Vehiculo crear(VehiculoRequest request) {
        Vehiculo vehiculo = new Vehiculo(request.patente(), request.tipo(), request.capacidadKg());
        return repository.save(vehiculo);
    }

    public List<Vehiculo> listarTodos() {
        return repository.findAll();
    }

    public Vehiculo buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("vehículo", id));
    }

    public Vehiculo cambiarDisponibilidad(Long id, boolean disponible) {
        Vehiculo vehiculo = buscarPorId(id);
        vehiculo.cambiarDisponibilidad(disponible);
        return repository.save(vehiculo);
    }
}
