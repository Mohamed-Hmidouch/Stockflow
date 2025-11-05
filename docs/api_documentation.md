# API Documentation - Stock Management System

## Base URL
`http://localhost:8080`

## Purchase Order Controller (`/api/purchase-orders`)

### 1. Create Purchase Order
**POST** `/api/purchase-orders/create`

**Description:** Créer une nouvelle commande d'achat

**Request Body:**
```json
{
  "supplierId": "123e4567-e89b-12d3-a456-426614174000",
  "status": "APPROVED",
  "lines": [
    {
      "productId": "123e4567-e89b-12d3-a456-426614174001",
      "quantity": 100,
      "unitPrice": 25.50
    },
    {
      "productId": "123e4567-e89b-12d3-a456-426614174002",
      "quantity": 50,
      "unitPrice": 15.75
    }
  ]
}
```

**Response:** `201 Created`
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174003",
  "supplier": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Supplier Name"
  },
  "status": "APPROVED",
  "lines": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174004",
      "product": {
        "id": "123e4567-e89b-12d3-a456-426614174001",
        "name": "Product 1"
      },
      "quantity": 100,
      "unitPrice": 25.50,
      "qtyReceived": 0
    }
  ],
  "createdAt": "2025-11-05T10:30:00Z"
}
```

### 2. Receive Purchase Order
**POST** `/api/purchase-orders/{orderId}/receive`

**Description:** Réceptionner une commande d'achat (partiellement ou complètement)

**Path Parameters:**
- `orderId`: UUID de la commande d'achat

**Request Body:**
```json
{
  "warehouseId": "123e4567-e89b-12d3-a456-426614174005",
  "receivedLineDto": [
    {
      "productId": 1,
      "poLine": 1,
      "quantityReceived": 80
    },
    {
      "productId": 2,
      "poLine": 2,
      "quantityReceived": 50
    }
  ]
}
```

**Response:** `200 OK`
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174003",
  "status": "PARTIALLY_RECEIVED",
  "lines": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174004",
      "product": {
        "id": "123e4567-e89b-12d3-a456-426614174001",
        "name": "Product 1"
      },
      "quantity": 100,
      "unitPrice": 25.50,
      "qtyReceived": 80
    }
  ]
}
```

### 3. Get All Purchase Orders
**GET** `/api/purchase-orders`

**Description:** Récupérer toutes les commandes d'achat

**Response:** `200 OK`
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174003",
    "supplier": {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Supplier Name"
    },
    "status": "APPROVED",
    "lines": [...],
    "createdAt": "2025-11-05T10:30:00Z"
  }
]
```

### 4. Get Purchase Order by ID
**GET** `/api/purchase-orders/{id}`

**Description:** Récupérer une commande d'achat par son ID

**Path Parameters:**
- `id`: UUID de la commande d'achat

**Response:** `200 OK`
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174003",
  "supplier": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Supplier Name"
  },
  "status": "APPROVED",
  "lines": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174004",
      "product": {
        "id": "123e4567-e89b-12d3-a456-426614174001",
        "name": "Product 1"
      },
      "quantity": 100,
      "unitPrice": 25.50,
      "qtyReceived": 0
    }
  ],
  "createdAt": "2025-11-05T10:30:00Z"
}
```

---

## Inventory Controller (`/api/inventory`)

### 1. Create Inventory Movement
**POST** `/api/inventory/movements/create`

**Description:** Créer un mouvement d'inventaire (INBOUND, OUTBOUND, ADJUSTMENT)

**Request Body:**
```json
{
  "productId": "123e4567-e89b-12d3-a456-426614174001",
  "warehouseId": "123e4567-e89b-12d3-a456-426614174005",
  "type": "INBOUND",
  "quantity": 100,
  "occurredAt": "2025-11-05T10:30:00Z",
  "referenceDoc": "PO-001"
}
```

**Movement Types:**
- `INBOUND`: Entrée de stock
- `OUTBOUND`: Sortie de stock
- `ADJUSTMENT`: Ajustement d'inventaire

**Response:** `201 Created`
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174006",
  "product": {
    "id": "123e4567-e89b-12d3-a456-426614174001",
    "name": "Product 1"
  },
  "warehouse": {
    "id": "123e4567-e89b-12d3-a456-426614174005",
    "name": "Warehouse 1"
  },
  "type": "INBOUND",
  "quantity": 100,
  "occurredAt": "2025-11-05T10:30:00Z",
  "referenceDoc": "PO-001"
}
```

### 2. Create Inventory Record
**POST** `/api/inventory`

**Description:** Créer un enregistrement d'inventaire initial

**Request Body:**
```json
{
  "qtyOnHand": 250,
  "qtyReserved": 50,
  "productId": "123e4567-e89b-12d3-a456-426614174001",
  "warehouseId": "123e4567-e89b-12d3-a456-426614174005"
}
```

**Response:** `201 Created`
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174007",
  "qtyOnHand": 250,
  "qtyReserved": 50,
  "qtyAvailable": 200,
  "product": {
    "id": "123e4567-e89b-12d3-a456-426614174001",
    "name": "Product 1"
  },
  "warehouse": {
    "id": "123e4567-e89b-12d3-a456-426614174005",
    "name": "Warehouse 1"
  }
}
```

### 3. Get All Inventories
**GET** `/api/inventory`

**Description:** Récupérer tous les enregistrements d'inventaire

**Response:** `200 OK`
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174007",
    "qtyOnHand": 250,
    "qtyReserved": 50,
    "qtyAvailable": 200,
    "product": {
      "id": "123e4567-e89b-12d3-a456-426614174001",
      "name": "Product 1"
    },
    "warehouse": {
      "id": "123e4567-e89b-12d3-a456-426614174005",
      "name": "Warehouse 1"
    }
  }
]
```

### 4. Get Inventories by Product
**GET** `/api/inventory/product/{productId}`

**Description:** Récupérer les inventaires pour un produit spécifique

**Path Parameters:**
- `productId`: UUID du produit

**Response:** `200 OK`
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174007",
    "qtyOnHand": 250,
    "qtyReserved": 50,
    "qtyAvailable": 200,
    "product": {
      "id": "123e4567-e89b-12d3-a456-426614174001",
      "name": "Product 1"
    },
    "warehouse": {
      "id": "123e4567-e89b-12d3-a456-426614174005",
      "name": "Warehouse 1"
    }
  }
]
```

### 5. Get Inventories by Warehouse
**GET** `/api/inventory/warehouse/{warehouseId}`

**Description:** Récupérer les inventaires pour un entrepôt spécifique

**Path Parameters:**
- `warehouseId`: UUID de l'entrepôt

**Response:** `200 OK`
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174007",
    "qtyOnHand": 250,
    "qtyReserved": 50,
    "qtyAvailable": 200,
    "product": {
      "id": "123e4567-e89b-12d3-a456-426614174001",
      "name": "Product 1"
    },
    "warehouse": {
      "id": "123e4567-e89b-12d3-a456-426614174005",
      "name": "Warehouse 1"
    }
  }
]
```

### 6. Get Inventory by ID
**GET** `/api/inventory/{id}`

**Description:** Récupérer un inventaire par son ID

**Path Parameters:**
- `id`: UUID de l'inventaire

**Response:** `200 OK`
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174007",
  "qtyOnHand": 250,
  "qtyReserved": 50,
  "qtyAvailable": 200,
  "product": {
    "id": "123e4567-e89b-12d3-a456-426614174001",
    "name": "Product 1"
  },
  "warehouse": {
    "id": "123e4567-e89b-12d3-a456-426614174005",
    "name": "Warehouse 1"
  }
}
```

### 7. Update Inventory
**PUT** `/api/inventory/{id}`

**Description:** Mettre à jour un enregistrement d'inventaire

**Path Parameters:**
- `id`: UUID de l'inventaire

**Request Body:**
```json
{
  "qtyOnHand": 300,
  "qtyReserved": 75,
  "productId": "123e4567-e89b-12d3-a456-426614174001",
  "warehouseId": "123e4567-e89b-12d3-a456-426614174005"
}
```

**Response:** `200 OK`
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174007",
  "qtyOnHand": 300,
  "qtyReserved": 75,
  "qtyAvailable": 225,
  "product": {
    "id": "123e4567-e89b-12d3-a456-426614174001",
    "name": "Product 1"
  },
  "warehouse": {
    "id": "123e4567-e89b-12d3-a456-426614174005",
    "name": "Warehouse 1"
  }
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2025-11-05T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/purchase-orders/create"
}
```

### 404 Not Found
```json
{
  "timestamp": "2025-11-05T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Purchase Order not found",
  "path": "/api/purchase-orders/123e4567-e89b-12d3-a456-426614174999"
}
```

### 409 Conflict
```json
{
  "timestamp": "2025-11-05T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Inventory already exists for product 'xxx' in warehouse 'yyy'",
  "path": "/api/inventory"
}
```

---

## Notes for Postman Collection

1. **UUIDs:** Utilisez des UUIDs valides pour tous les IDs (format: `123e4567-e89b-12d3-a456-426614174000`)
2. **Timestamps:** Format ISO-8601 avec timezone UTC (ex: `2025-11-05T10:30:00Z`)
3. **Enum Values:**
   - POStatus: `DRAFT`, `APPROVED`, `PARTIALLY_RECEIVED`, `RECEIVED`, `CANCELED`
   - MovementType: `INBOUND`, `OUTBOUND`, `ADJUSTMENT`
4. **Content-Type:** `application/json` pour tous les requests POST/PUT
5. **Authorization:** Ajoutez les headers d'authentification si nécessaire

## Collection Structure Recommandée

```
📁 Stock Management API
├── 📁 Purchase Orders
│   ├── 📄 Create Purchase Order (POST)
│   ├── 📄 Receive Purchase Order (POST)
│   ├── 📄 Get All Purchase Orders (GET)
│   └── 📄 Get Purchase Order by ID (GET)
└── 📁 Inventory
    ├── 📄 Create Inventory Movement (POST)
    ├── 📄 Create Inventory Record (POST)
    ├── 📄 Get All Inventories (GET)
    ├── 📄 Get Inventories by Product (GET)
    ├── 📄 Get Inventories by Warehouse (GET)
    ├── 📄 Get Inventory by ID (GET)
    └── 📄 Update Inventory (PUT)
```