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

## Mensajería con RabbitMQ (EP2)

Catalog **consume comandos** de shipments y **publica eventos** con el resultado.
Variables de conexión: `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`,
`RABBITMQ_PASSWORD` (mismas credenciales que `docker/.env` del repo
`ms-rutaexpress-rabbit-admin`).

### Flujo

```
shipments ──(rk envio.asignar-vehiculo)──▶ rutaexpress.commands (direct)
                                              │
                                              ▼
                                   q.envio.comandos ──────────────┐ rechazado / vencido / desbordado
                                              │                    ▼
                          ComandoEnvioListener (ACK manual)   rutaexpress.dlx ──▶ q.envio.comandos.dlq
                                              │                                      │
                          ACK + evento        ▼                                      ▼
   rutaexpress.events (topic) ◀── rk envio.vehiculo.asignado           DeadLetterListener (log)
```

### Contrato de mensajes (shipments debe usar lo mismo)

| Mensaje | Exchange | Routing key | Cuerpo JSON |
|---|---|---|---|
| Comando | `rutaexpress.commands` | `envio.asignar-vehiculo` | `{"envioId":1,"trackingCode":"RE-AB12CD34","pesoKg":300}` |
| Evento | `rutaexpress.events` | `envio.vehiculo.asignado` | `{"envioId":1,"trackingCode":"RE-AB12CD34","vehiculoId":3,"patente":"ABCD-12","ocurridoEn":"2026-10-16T15:00:00Z"}` |

### Dónde está cada cosa

| Pieza | Archivo |
|---|---|
| Nombres, routing keys y políticas | `application.yml` (`rutaexpress.mensajeria.*`) |
| Beans `Queue`, `Exchange` y `Binding` | `config/RabbitTopologyConfig` |
| Conversión a JSON | `config/RabbitMessagingConfig` |
| Regla de negocio (reservar vehículo) | `service/AsignacionVehiculoService` |
| Consumidor de comandos | `messaging/ComandoEnvioListener` |
| Consumidor de la DLQ (log) | `messaging/DeadLetterListener` |
| Productor de eventos | `messaging/EventoPublisher` |

La lógica de negocio no sabe nada de RabbitMQ, y la configuración de la mensajería no contiene lógica de negocio.

### Cómo se maneja cada error

| Situación | Tipo | Resultado |
|---|---|---|
| Éxito | — | ACK, luego se publica el evento |
| El cuerpo no es JSON | No recuperable | NACK sin requeue → DLQ |
| Comando incompleto o `pesoKg` ≤ 0 | No recuperable | NACK sin requeue → DLQ |
| No hay vehículo con capacidad, o error de BD | Recuperable | Hasta 3 intentos (500 ms, 1000 ms entre ellos); si persiste, NACK sin requeue → DLQ |
| Comando sin atender por 5 min | — | Expira (`x-message-ttl`) → DLQ |
| Cola con 10.000 mensajes | — | El más antiguo se desborda (`drop-head`) → DLQ |

Los mensajes de la DLQ quedan en el log como `[DLQ] Mensaje no entregado. colaOrigen=... motivo=... contenido=...`.
La propia DLQ conserva 7 días y máximo 10.000 mensajes, así que tampoco crece sin límite.

### Probarlo en el Management UI

Con catalog y RabbitMQ arriba, en **Exchanges → rutaexpress.commands → Publish message**
(routing key `envio.asignar-vehiculo`):

1. **Éxito** (con un vehículo de 800 kg disponible):
   `{"envioId":1,"trackingCode":"RE-TEST0001","pesoKg":300}`
   → log "Vehículo ... asignado", cola vacía, evento en `rutaexpress.events`.
2. **No recuperable**: `{"envioId":1,"trackingCode":"RE-TEST0002","pesoKg":-5}`
   → `Comando inválido (no recuperable)` y `[DLQ] ... motivo=rejected`.
3. **Mensaje basura**: `esto no es json` → `Comando ilegible` y `[DLQ]`.
4. **Recuperable**: `{"envioId":1,"trackingCode":"RE-TEST0003","pesoKg":99999}`
   → 3 `Intento n/3 fallido` y luego `[DLQ]`.

### Notas para la defensa

- **Prefetch = 10** (`spring.rabbitmq.listener.simple.prefetch`): RabbitMQ no entrega más de 10 mensajes sin confirmar por consumidor. Sin límite, un consumidor lento acumularía miles de mensajes en memoria mientras otros consumidores quedan ociosos.
- **ACK manual**: el mensaje solo sale de la cola cuando el trabajo terminó; si el proceso cae antes del ACK, RabbitMQ lo reentrega.
- **ACK y luego evento**: si la publicación del evento falla después del ACK, el fallo se registra en el log y no se reasigna otro vehículo. La solución completa sería un patrón *outbox*.
- **Bloqueo pesimista** en la búsqueda de vehículo: dos consumidores concurrentes no pueden reservar el mismo vehículo.

## Pruebas

```bash
mvn test
```

Las pruebas de mensajería usan mocks y no necesitan RabbitMQ; en el perfil `test` los listeners no arrancan.
