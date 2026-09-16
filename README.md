# ms-rutaexpress-catalog

Microservicio de catálogo: servicios de envío (con tarifas) y capacidad de
flota (vehículos). Igual que `shipments`, no valida JWT por su cuenta —
confía en que solo el BFF lo llama.

## Configurar

1. Copia `src/main/resources/application-local.yml.example` a
   `application-local.yml` (gitignored).
2. Pega tu cadena de Neon — puede ser el **mismo** proyecto que usaste para
   `shipments`, las tablas (`servicios_envio`, `vehiculos`) no chocan con
   `envios`.

## Correr

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Levanta en `http://localhost:8083`.

## Probar con curl

```bash
# Crear un servicio de envío
curl -X POST http://localhost:8083/servicios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Express","descripcion":"Entrega en 24 horas","tarifaBase":5990,"tiempoEstimadoHoras":24}'

# Listar servicios
curl http://localhost:8083/servicios

# Crear un vehículo
curl -X POST http://localhost:8083/vehiculos \
  -H "Content-Type: application/json" \
  -d '{"patente":"ABCD-12","tipo":"FURGON","capacidadKg":800}'

# Marcarlo como no disponible
curl -X PATCH http://localhost:8083/vehiculos/1/disponibilidad \
  -H "Content-Type: application/json" \
  -d '{"disponible":false}'
```

## Pruebas

```bash
mvn test
```

Con H2 en memoria (perfil `test`): crea un servicio, rechaza una tarifa
negativa (400), crea un vehículo y cambia su disponibilidad, y confirma 404
para un vehículo inexistente.

## Siguiente paso

Con `shipments` y `catalog` ambos arriba, lo que sigue es que el BFF exponga
controllers que reenvíen hacia los dos — recién ahí el dashboard deja de
decir "Próximamente".
