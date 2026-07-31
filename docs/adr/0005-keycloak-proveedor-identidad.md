# ADR-0005: Keycloak como proveedor de identidad externo (OAuth2/OIDC)

## Estado
Aceptada — 30 de julio de 2026

## Contexto
El acta exige OAuth2, OpenID Connect, JWT y Keycloak. Hace falta decidir
cómo se integra Keycloak sin acoplar la lógica de negocio de Identity/Auth
Service a los detalles de un proveedor externo.

## Decisión
Keycloak se integra en dos puntos distintos, sin mezclarlos:

1. **Validación de tokens (transversal a todos los servicios)**: cada
   microservicio actúa como OAuth2 Resource Server de Spring Security,
   validando el JWT contra el JWK Set de Keycloak vía `issuer-uri`. El
   login (usuario/contraseña) lo negocia el cliente (app/web) directamente
   con Keycloak mediante Authorization Code + PKCE; ningún backend de
   NextBank ve ni gestiona contraseñas.
2. **Aprovisionamiento e identidad (puerto de salida en Identity/Auth
   Service)**: una interfaz `IdentityProviderPort` con una implementación
   `KeycloakIdentityProviderAdapter` (usando el Keycloak Admin Client) para
   crear usuarios, asignar roles y sincronizar estado al completar el alta
   o el KYC.

El agregado `Customer` guarda el `sub` del JWT como `keycloakUserId`, no
como identificador propio — el dominio referencia a Keycloak, no depende de
él.

## Consecuencias

### Positivas
- Ningún servicio de NextBank almacena ni gestiona contraseñas.
- El dominio (`Customer`) queda desacoplado del proveedor concreto: si en
  el futuro se cambia Keycloak por otro IdP, solo cambia el adaptador.
- La validación de tokens es transversal y gratuita para cualquier servicio
  nuevo que se añada (solo requiere la configuración de Resource Server).

### Negativas
- Dependencia operativa de un Keycloak disponible y bien configurado
  (realm, clientes, protocol mappers) desde el principio del desarrollo.
- Añade infraestructura extra (Keycloak + su base de datos) al entorno
  local/CI.

## Alternativas consideradas
- **Gestión de usuarios y contraseñas propia** (sin IdP externo):
  descartada porque el acta pide explícitamente Keycloak, y porque
  reimplementar gestión de credenciales de forma segura no aporta valor de
  aprendizaje adicional relevante para el objetivo del proyecto.
