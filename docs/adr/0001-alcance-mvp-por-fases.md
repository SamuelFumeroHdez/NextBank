# ADR-0001: Alcance del MVP limitado a 4 microservicios, desarrollo por fases

## Estado
Aceptada — 30 de julio de 2026

## Contexto
El Acta de Constitución de NextBank define un alcance completo de 17 dominios
(Customer, Identity, Authentication, Authorization, Account, Card, Payments,
Transfers, Loans, Investments, Notifications, Documents, Audit, Risk, Fraud
Detection, Reporting, Gateway API). Desarrollar los 17 en paralelo desde el
inicio implica un proyecto del tamaño de un equipo de 6-10 personas durante
~1 año, incompatible con el objetivo real: tener un portfolio publicable en
un plazo razonable y defendible en un proceso de entrevista técnica.

## Decisión
Se desarrolla primero un núcleo de **4 microservicios**: Identity/Auth,
Account, Transfers/Payments y Notifications. Se publica como portfolio en
ese punto, y a partir de ahí se expande el alcance hacia el resto de
dominios definidos en el acta.

## Consecuencias

### Positivas
- Permite alcanzar profundidad real (patrones bien implementados) en vez de
  amplitud superficial en 17 servicios a medio hacer.
- Da un hito de publicación concreto y alcanzable en el corto plazo.
- Los 4 servicios elegidos cubren, entre sí, la mayoría de los objetivos
  técnicos del acta (Hexagonal, DDD, Event-Driven, Saga, Outbox, Circuit
  Breaker vía Account Client), por lo que siguen siendo representativos del
  proyecto completo.

### Negativas
- El resto del alcance (Cards, Loans, Investments, Risk, Fraud Detection,
  Reporting, Documents, Audit) queda pendiente y no se demuestra en la
  primera versión publicada.
- Riesgo de que la arquitectura base tenga que ajustarse al añadir dominios
  con necesidades distintas (p. ej. Risk/Fraud, que probablemente requiera
  CQRS o Event Sourcing de forma más intensiva que los 4 iniciales).

## Alternativas consideradas
- **Desarrollar los 17 servicios en paralelo desde el inicio**: descartada
  por desproporcionada para un proyecto individual con plazo de entrevista.
- **Un monolito modular en vez de microservicios**: descartada porque el
  objetivo explícito del acta es demostrar competencia en arquitectura
  distribuida, que es justo lo que se evalúa en el proceso de Revolut.
