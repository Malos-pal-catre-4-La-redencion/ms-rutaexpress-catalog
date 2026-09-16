package com.rutaexpress.catalog.web;

import com.rutaexpress.catalog.model.Vehiculo;
import com.rutaexpress.catalog.service.VehiculoService;
import com.rutaexpress.catalog.web.dto.CambiarDisponibilidadRequest;
import com.rutaexpress.catalog.web.dto.VehiculoRequest;
import com.rutaexpress.catalog.web.dto.VehiculoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehiculos")
public class VehiculoController {

    private final VehiculoService service;

    public VehiculoController(VehiculoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<VehiculoResponse> crear(@Valid @RequestBody VehiculoRequest request) {
        Vehiculo creado = service.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(VehiculoResponse.desde(creado));
    }

    @GetMapping
    public List<VehiculoResponse> listar() {
        return service.listarTodos().stream().map(VehiculoResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public VehiculoResponse obtener(@PathVariable Long id) {
        return VehiculoResponse.desde(service.buscarPorId(id));
    }

    @PatchMapping("/{id}/disponibilidad")
    public VehiculoResponse cambiarDisponibilidad(@PathVariable Long id, @Valid @RequestBody CambiarDisponibilidadRequest request) {
        return VehiculoResponse.desde(service.cambiarDisponibilidad(id, request.disponible()));
    }
}
