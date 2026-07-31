# ADR-0003: Saga orquestada en Transfers, con comandos síncronos vía REST hacia Account

## Estado
Aceptada — 30 de julio de 2026

## Contexto
Una transferencia implica coordinar un débito en la cuenta origen y un
crédito en la cuenta destino, que viven en el agregado `Account` (Account
Service), fuera de la transacción local de Transfers Service. Hace falta
garantizar consistencia sin transacciones distribuidas (2PC), y decidir si
esa coordinación se hace vía eventos asíncronos o vía llamadas síncronas.

## Decisión
- La coordinación del flujo (débito → crédito → notificación, con
  compensación si algo falla) la lleva un **Saga Orchestrator** dentro de
  Transfers Service — no una saga coreografiada entre servicios.
- El débito/crédito contra Account Service se invoca **síncronamente por
  REST** (`Account Client`, ej. `POST /accounts/{id}/debit`), no mediante
  eventos, porque la saga necesita el resultado inmediato (éxito/fallo)
  para decidir el siguiente paso o iniciar la compensación.
- Los eventos de dominio (`transfer.transfer-initiated.v1`,
  `transfer.transfer-completed.v1`, `transfer.transfer-failed.v1`) se
  publican para informar a otros servicios (Notifications) de hechos ya
  consumados, nunca como mecanismo para ejecutar el débito/crédito.

## Consecuencias

### Positivas
- Fácil de razonar y depurar: todo el flujo de la transferencia vive en un
  único componente (el orquestador), en vez de repartirse implícitamente
  entre varios servicios que reaccionan a eventos.
- Separación clara entre "comando que necesita respuesta" (REST) y "hecho
  que otros deben saber" (evento) — evita el error común de intentar
  ejecutar lógica de negocio crítica a través de un bus de eventos
  asíncrono.

### Negativas
- Account Service pasa a ser una dependencia síncrona dura para Transfers:
  si Account no responde, la transferencia no puede avanzar (se mitiga con
  Circuit Breaker y Retry, ya contemplados en el acta).
- El orquestador concentra bastante responsabilidad; si el número de pasos
  de la saga crece mucho, puede volverse un componente grande a mantener.

## Alternativas consideradas
- **Saga coreografiada** (cada servicio reacciona a eventos del anterior,
  sin orquestador central): descartada para el MVP por ser más difícil de
  seguir y depurar con solo 2-3 pasos en el flujo; se revalorará si el
  número de servicios implicados en una transferencia crece.
- **Débito/crédito vía eventos asíncronos**: descartada porque la saga
  necesita conocer el resultado para decidir compensar o continuar, y un
  evento no da esa garantía de respuesta inmediata.
