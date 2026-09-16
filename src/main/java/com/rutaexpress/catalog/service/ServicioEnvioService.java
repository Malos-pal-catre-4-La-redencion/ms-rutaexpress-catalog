package com.rutaexpress.catalog.service;

import com.rutaexpress.catalog.exception.RecursoNoEncontradoException;
import com.rutaexpress.catalog.model.ServicioEnvio;
import com.rutaexpress.catalog.repository.ServicioEnvioRepository;
import com.rutaexpress.catalog.web.dto.ServicioRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioEnvioService {

    private final ServicioEnvioRepository repository;

    public ServicioEnvioService(ServicioEnvioRepository repository) {
        this.repository = repository;
    }

    public ServicioEnvio crear(ServicioRequest request) {
        ServicioEnvio servicio = new ServicioEnvio(
                request.nombre(), request.descripcion(), request.tarifaBase(), request.tiempoEstimadoHoras());
        return repository.save(servicio);
    }

    public List<ServicioEnvio> listarTodos() {
        return repository.findAll();
    }

    public ServicioEnvio buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("servicio", id));
    }

    public ServicioEnvio actualizar(Long id, ServicioRequest request) {
        ServicioEnvio servicio = buscarPorId(id);
        servicio.actualizar(request.nombre(), request.descripcion(), request.tarifaBase(), request.tiempoEstimadoHoras());
        return repository.save(servicio);
    }

    public void desactivar(Long id) {
        ServicioEnvio servicio = buscarPorId(id);
        servicio.desactivar();
        repository.save(servicio);
    }
}
