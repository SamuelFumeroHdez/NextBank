# ADR-0007: Avro + Confluent Schema Registry para el payload de los eventos

## Estado
Aceptada — 30 de julio de 2026

## Contexto
Con varios servicios productores y consumidores de eventos, hace falta
decidir el formato de serialización del payload y cómo se garantiza que un
cambio en el productor no rompa a los consumidores existentes.

## Decisión
Los payloads de los eventos se serializan con **Avro**, registrados en un
**Confluent Schema Registry**, en vez de JSON sin esquema. Regla de
evolución: solo se permiten cambios aditivos con valor por defecto
(`"default": null`) sobre un schema ya publicado; nunca se borra ni se
renombra un campo existente — se deprecia y, si hace falta romper
compatibilidad, se crea una nueva versión (`v2`) del evento.

## Consecuencias

### Positivas
- La compatibilidad se fuerza en tiempo de *build/publish* (el registry
  rechaza un schema que rompa el contrato), no se descubre en producción.
- Formato compacto y tipado, alineado con lo habitual en stacks fintech de
  referencia (Revolut, Monzo-style), relevante para el objetivo del
  proyecto como portfolio de entrevista.

### Negativas
- Añade una pieza de infraestructura más (Schema Registry) al entorno
  local/CI y a los manifiestos de despliegue.
- Curva de aprendizaje adicional para quien no haya trabajado antes con
  Avro y gestión de compatibilidad de schemas.

## Alternativas consideradas
- **JSON sin esquema**: descartada como formato definitivo por no forzar
  compatibilidad de ningún modo verificable; quedaría como opción de menor
  fricción si en algún momento se decide posponer el Schema Registry, a
  costa de perder esa garantía de compatibilidad en build-time.
