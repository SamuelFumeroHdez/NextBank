# ADR-0002: Arquitectura hexagonal (puertos y adaptadores) en cada microservicio

## Estado
Aceptada — 30 de julio de 2026

## Contexto
El acta exige demostrar Arquitectura Hexagonal, DDD y Clean Architecture.
Se necesita una estructura interna consistente y repetible en los 4
servicios del MVP, para que el diseño sea predecible tanto al desarrollar
como al defenderlo en una entrevista.

## Decisión
Cada microservicio se estructura en 4 capas:
- **Adaptadores de entrada**: REST Controller (Identity/Auth, Account,
  Transfers) o Kafka Consumer (Notifications).
- **Capa de aplicación**: casos de uso / orquestación (en Transfers, un
  Saga Orchestrator en vez de un caso de uso simple).
- **Modelo de dominio**: agregados y reglas de negocio, sin dependencias de
  frameworks ni de infraestructura.
- **Adaptadores de salida** (puertos): repositorios (PostgreSQL), Outbox
  (Kafka), clientes a otros servicios (Account Client) y adaptadores
  externos (Keycloak, Email/Push/SMS simulados).

Los diagramas C4-3 de los 4 servicios (Identity/Auth, Account, Transfers,
Notifications) documentan esta estructura de forma consistente.

## Consecuencias

### Positivas
- El dominio queda aislado de frameworks e infraestructura, facilitando
  testear la lógica de negocio con JUnit/Mockito sin levantar Spring.
- Estructura repetible entre servicios: quien lea uno, entiende los demás.
- Facilita sustituir adaptadores (p. ej. cambiar Keycloak por otro IdP, o
  Postgres por otro motor) sin tocar el dominio.

### Negativas
- Más código de "cableado" (interfaces de puertos + implementaciones) que
  un enfoque en capas más simple (controller → service → repository).
- Curva de entrada algo mayor si en el futuro se incorpora a alguien al
  proyecto sin experiencia previa en el patrón.

## Alternativas consideradas
- **Arquitectura en capas clásica (N-tier)**: descartada porque no separa
  con la misma claridad el dominio de la infraestructura, y el acta pide
  explícitamente demostrar Hexagonal/Clean Architecture.
