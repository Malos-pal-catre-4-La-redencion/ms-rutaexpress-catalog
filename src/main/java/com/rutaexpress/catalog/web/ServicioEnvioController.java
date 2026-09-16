package com.rutaexpress.catalog.web;

import com.rutaexpress.catalog.model.ServicioEnvio;
import com.rutaexpress.catalog.service.ServicioEnvioService;
import com.rutaexpress.catalog.web.dto.ServicioRequest;
import com.rutaexpress.catalog.web.dto.ServicioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/servicios")
public class ServicioEnvioController {

    private final ServicioEnvioService service;

    public ServicioEnvioController(ServicioEnvioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ServicioResponse> crear(@Valid @RequestBody ServicioRequest request) {
        ServicioEnvio creado = service.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ServicioResponse.desde(creado));
    }

    @GetMapping
    public List<ServicioResponse> listar() {
        return service.listarTodos().stream().map(ServicioResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public ServicioResponse obtener(@PathVariable Long id) {
        return ServicioResponse.desde(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ServicioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ServicioRequest request) {
        return ServicioResponse.desde(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
