## Inventory API Endpoints

Base URL: `http://localhost:8080/api/inventory`

### Inventaire (CRUD et requêtes)

- POST `/api/inventory`
  Body:
```json
{
  "productId": "UUID",
  "warehouseId": "UUID",
  "qtyOnHand": 100,
  "qtyReserved": 0
}
```

- GET `/api/inventory`

- GET `/api/inventory/{id}`

- GET `/api/inventory/product/{productId}`

- GET `/api/inventory/warehouse/{warehouseId}`

- GET `/api/inventory/product/{productId}/warehouse/{warehouseId}`

- GET `/api/inventory/summary/product/{productId}`

- PUT `/api/inventory/{id}`
  Body:
```json
{
  "productId": "UUID (optionnel si pas de changement)",
  "warehouseId": "UUID (optionnel si pas de changement)",
  "qtyOnHand": 120,
  "qtyReserved": 10
}
```

- DELETE `/api/inventory/{id}`

---

### Mouvements

- POST `/api/inventory/movements/create`
  Body (INBOUND):
```json
{
  "productId": "UUID",
  "warehouseId": "UUID",
  "type": "INBOUND",
  "quantity": 50,
  "referenceDoc": "PO-2025-001",
  "occurredAt": "2025-10-31T10:00:00Z"
}
```

  Body (OUTBOUND):
```json
{
  "productId": "UUID",
  "warehouseId": "UUID",
  "type": "OUTBOUND",
  "quantity": 10,
  "referenceDoc": "SO-2025-010"
}
```

  Body (ADJUSTMENT positif ou négatif):
```json
{
  "productId": "UUID",
  "warehouseId": "UUID",
  "type": "ADJUSTMENT",
  "quantity": -2,
  "referenceDoc": "COUNT-2025-03"
}
```

- GET `/api/inventory/movements`
  Query params (optionnels): `productId`, `warehouseId`, `type`, `from`, `to`, `page`, `size`
  Exemple:
```
/api/inventory/movements?productId=UUID&warehouseId=UUID&type=INBOUND&page=0&size=20
```

---

### Flux fonctionnels (DTOs dédiés)

- POST `/api/inventory/inbound`
  Body:
```json
{
  "productId": "UUID",
  "warehouseId": "UUID",
  "quantity": 50,
  "referenceDoc": "PO-2025-001",
  "occurredAt": "2025-10-31T10:00:00Z"
}
```

- POST `/api/inventory/adjustments`
  Body:
```json
{
  "productId": "UUID",
  "warehouseId": "UUID",
  "quantity": -2,
  "reason": "Stock cassé",
  "referenceDoc": "ADJ-2025-07",
  "occurredAt": "2025-10-31T11:00:00Z"
}
```

- POST `/api/inventory/reservations`
  Body:
```json
{
  "salesOrderId": "UUID"
}
```

- POST `/api/inventory/reservations/release`
  Body:
```json
{
  "salesOrderId": "UUID"
}
```

- POST `/api/inventory/outbound`
  Body:
```json
{
  "salesOrderId": "UUID",
  "shipmentId": "UUID"
}
```

---

### Transfert entre entrepôts

- POST `/api/inventory/transfer`
  Body:
```json
{
  "productId": "UUID",
  "fromWarehouseId": "UUID",
  "toWarehouseId": "UUID",
  "quantity": 20,
  "referenceDoc": "TRF-2025-02"
}
```

---

Notes:
- Remplacer `UUID` par vos identifiants réels.
- `occurredAt` est optionnel (défaut: now, format ISO-8601 UTC).
- Les erreurs métier lèvent `BusinessRuleException` et sont gérées par le handler global (400/409).
