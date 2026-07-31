# ADR-0004: Outbox Pattern para la publicación de eventos en Kafka

## Estado
Aceptada — 30 de julio de 2026

## Contexto
Account y Transfers necesitan que un cambio de estado en base de datos
(cuenta abierta, transferencia completada) y la publicación del evento
correspondiente en Kafka ocurran de forma consistente. Escribir en
PostgreSQL y publicar en Kafka como dos operaciones independientes puede
dejar el sistema inconsistente si una de las dos falla (evento perdido, o
evento publicado sin el cambio de estado persistido).

## Decisión
Cada servicio que publica eventos (Account, Transfers) implementa el
**Outbox Pattern**: el evento se escribe en una tabla `outbox` dentro de la
misma transacción de base de datos que el cambio de estado del agregado. Un
proceso aparte (poller o Debezium/CDC) lee la tabla outbox y publica a
Kafka de forma asíncrona, marcando el registro como enviado.

## Consecuencias

### Positivas
- Consistencia garantizada entre el estado del agregado y el evento
  publicado, sin necesidad de transacciones distribuidas.
- El evento se persiste antes de intentar publicarlo: un fallo temporal de
  Kafka no provoca pérdida de eventos, solo retraso.

### Negativas
- Introduce latencia entre el cambio de estado y la publicación real en
  Kafka (no es instantáneo).
- Añade una tabla y un proceso adicional (poller) por cada servicio que
  publica eventos, más superficie que mantener.

## Alternativas consideradas
- **Publicar directamente a Kafka tras el commit en base de datos** (sin
  Outbox): descartada por el riesgo de inconsistencia si la publicación
  falla justo después del commit.
- **Event Sourcing puro** (el propio log de eventos es la fuente de
  verdad): valorado para el histórico de movimientos de Account, pero
  descartado como mecanismo general del MVP por la complejidad añadida;
  queda como posible evolución futura señalada en el acta.
