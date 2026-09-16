package com.rutaexpress.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void creaUnServicioDeEnvio() throws Exception {
        Map<String, Object> request = Map.of(
                "nombre", "Express",
                "descripcion", "Entrega en 24 horas",
                "tarifaBase", new BigDecimal("5990"),
                "tiempoEstimadoHoras", 24
        );

        mockMvc.perform(post("/servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Express"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void rechazaTarifaNegativa() throws Exception {
        Map<String, Object> request = Map.of(
                "nombre", "Express",
                "tarifaBase", new BigDecimal("-100"),
                "tiempoEstimadoHoras", 24
        );

        mockMvc.perform(post("/servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creaUnVehiculoYCambiaSuDisponibilidad() throws Exception {
        Map<String, Object> request = Map.of(
                "patente", "ABCD-12",
                "tipo", "FURGON",
                "capacidadKg", new BigDecimal("800")
        );

        String respuesta = mockMvc.perform(post("/vehiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(respuesta).get("id").asLong();

        mockMvc.perform(patch("/vehiculos/{id}/disponibilidad", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("disponible", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(false));
    }

    @Test
    void vehiculoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/vehiculos/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
