# Catálogo de eventos de dominio

Documento vivo — se actualiza cada vez que se añade, versiona o deprecia un
evento. La forma del envelope común está fijada en
[ADR-0006](../adr/0006-contrato-eventos-envelope.md); aquí solo se listan
los payloads concretos.

Cada evento va serializado en Avro y registrado en el Schema Registry bajo
el subject `<topic>-value`, siguiendo la regla de evolución de
[ADR-0007](../adr/0007-avro-schema-registry.md).

---

## `account.account-opened.v1`

| | |
|---|---|
| Publica | Account Service |
| Topic | `account.events` |
| Partition key | `accountId` |
| Consume | Notifications |

```json
{
  "accountId": "acc_7f3a...",
  "customerId": "cus_1a2b...",
  "iban": "ES9121000418450200051332",
  "currency": "EUR",
  "openedAt": "2026-07-30T10:00:00Z"
}
```

## `transfer.transfer-initiated.v1`

| | |
|---|---|
| Publica | Transfers Service |
| Topic | `transfer.events` |
| Partition key | `transferId` |
| Consume | Notifications |

```json
{
  "transferId": "trf_9f8e...",
  "sourceAccountId": "acc_7f3a...",
  "destinationIban": "ES7620770024003102575766",
  "amount": 150.00,
  "currency": "EUR",
  "initiatedAt": "2026-07-30T10:15:00Z"
}
```

## `transfer.transfer-completed.v1`

| | |
|---|---|
| Publica | Transfers Service |
| Topic | `transfer.events` |
| Partition key | `transferId` |
| Consume | Notifications |

```json
{
  "transferId": "trf_9f8e...",
  "sourceAccountId": "acc_7f3a...",
  "destinationIban": "ES7620770024003102575766",
  "amount": 150.00,
  "currency": "EUR",
  "completedAt": "2026-07-30T10:15:04Z"
}
```

## `transfer.transfer-failed.v1`

| | |
|---|---|
| Publica | Transfers Service |
| Topic | `transfer.events` |
| Partition key | `transferId` |
| Consume | Notifications |

```json
{
  "transferId": "trf_9f8e...",
  "sourceAccountId": "acc_7f3a...",
  "reason": "DESTINATION_ACCOUNT_NOT_FOUND",
  "compensated": true,
  "failedAt": "2026-07-30T10:15:06Z"
}
```

`reason` es un enum cerrado (`DESTINATION_ACCOUNT_NOT_FOUND`,
`INSUFFICIENT_FUNDS`, `DAILY_LIMIT_EXCEEDED`, ...), no texto libre.
