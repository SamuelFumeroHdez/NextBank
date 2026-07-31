# ADR-0006: Envelope común y versionado para los eventos de dominio

## Estado
Aceptada — 30 de julio de 2026

## Contexto
Con varios servicios publicando y consumiendo eventos (Account y Transfers
como productores, Notifications como consumidor, y más servicios previstos
en fases futuras), hace falta un formato común que permita trazabilidad
entre servicios y evolución del contrato sin romper consumidores existentes.

## Decisión
Todo evento publicado en Kafka usa el mismo envelope, con el contenido
específico de cada evento en el campo `payload`:

- `eventId`: identificador único del evento, usado por el consumidor para
  deduplicar (idempotencia).
- `eventType`: `<dominio>.<evento-en-kebab>.v<versión>`
  (ej. `transfer.transfer-completed.v1`) — la versión va en el nombre, no
  solo en el schema.
- `occurredAt`, `aggregateId`, `aggregateType`, `producer`.
- `correlationId`: común a todos los eventos de una misma operación de
  negocio, propagado también por header HTTP (`X-Correlation-Id`) entre
  llamadas síncronas.
- `causationId`: referencia al evento/comando que causó este evento.

```json
{
  "eventId": "018f2c3a-...",
  "eventType": "<dominio>.<evento-en-kebab>.v<version>",
  "occurredAt": "2026-07-30T10:15:32.481Z",
  "aggregateId": "...",
  "aggregateType": "...",
  "correlationId": "018f2c39-...",
  "causationId": "018f2c38-...",
  "producer": "<nombre-del-servicio>",
  "payload": { }
}
```

Cada payload incluye solo los campos que el consumidor necesita para
actuar, no el estado interno completo del agregado. El catálogo vivo de
eventos concretos (con su payload exacto) se mantiene aparte, en
`docs/events/catalog.md`, no en este ADR — ese catálogo crece con cada
evento nuevo, mientras que este documento registra la decisión sobre la
*forma* del contrato, no su contenido en un momento dado.

## Consecuencias

### Positivas
- Trazabilidad end-to-end de una operación de negocio a través de servicios
  y de la frontera síncrono/asíncrono, gracias a `correlationId`.
- Los consumidores pueden deduplicar de forma genérica usando `eventId`,
  sin lógica específica por tipo de evento.
- Los payloads reducidos minimizan el acoplamiento: cambios internos en el
  productor que no afectan a los campos expuestos no rompen consumidores.

### Negativas
- Disciplina adicional: cada nuevo evento debe respetar el envelope y
  justificar qué campos van en el payload, en vez de serializar el
  agregado completo por comodidad.

## Alternativas consideradas
- **Payload "plano" sin envelope común** (cada evento con su propia
  estructura): descartada porque obliga a cada consumidor a implementar su
  propia lógica de trazabilidad y deduplicación.
