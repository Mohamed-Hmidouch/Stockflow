# 📋 Architecture Service Layer - Règles Métier Complètes

> **Note**: Utilisation de ModelMapper pour les conversions Entity ↔ DTO

---

## 📁 Structure des Services

```
src/main/java/com/example/stockgestion/services/
├── ProductService.java
├── WarehouseService.java
├── InventoryService.java
├── ClientService.java
├── UserService.java
├── SupplierService.java
├── CarrierService.java
├── SalesOrderService.java
├── PurchaseOrderService.java
├── ShipmentService.java
└── ReportingService.java
```



## 3️⃣ InventoryService.java

**Localisation**: `src/main/java/com/example/stockgestion/services/InventoryService.java`


### Méthodes Métier Manquantes - CRITIQUE
```java
// US6 - Enregistrer une ENTRÉE de stock (simplifié)
@Transactional
InventoryMovementResponseDto recordInbound(InboundRequestDto dto);

// US7 - Enregistrer une SORTIE de stock (avec vérification stricte)
@Transactional
InventoryMovementResponseDto recordOutbound(OutboundRequestDto dto);

// US8 - Ajuster le stock (avec validation qtyOnHand >= qtyReserved)
@Transactional
InventoryMovementResponseDto recordAdjustment(AdjustmentRequestDto dto);

// US9 - RÉSERVER le stock pour une commande (RÈGLE CRITIQUE)
@Transactional
void reserveStock(UUID productId, UUID warehouseId, long quantity);

// US9 - LIBÉRER une réservation
@Transactional
void releaseReservation(UUID productId, UUID warehouseId, long quantity);

// US4 - Allocation multi-entrepôts (si stock insuffisant dans un entrepôt)
@Transactional
List<InventoryAllocationDto> allocateStockAcrossWarehouses(UUID productId, long requestedQty);

// Calculer la disponibilité (qtyOnHand - qtyReserved)
@Transactional(readOnly = true)
long getAvailableStock(UUID productId, UUID warehouseId);

// Vérifier si le stock est suffisant
@Transactional(readOnly = true)
boolean isStockAvailable(UUID productId, UUID warehouseId, long quantity);


// US4 - Récupérer tous les inventaires d'un produit triés par disponibilité (DESC)
@Transactional(readOnly = true)
List<InventoryResponseDto> getInventoriesByProductSortedByAvailability(UUID productId);
```

---

## 4️⃣ ClientService.java

**Localisation**: `src/main/java/com/example/stockgestion/services/ClientService.java`

### Méthodes CRUD de Base
```java
// US1 - Créer un client
@Transactional
ClientResponseDto createClient(ClientRequestDto dto);

@Transactional(readOnly = true)
ClientResponseDto getClientById(UUID id);

@Transactional(readOnly = true)
List<ClientResponseDto> getAllClients();

@Transactional
ClientResponseDto updateClient(UUID id, ClientRequestDto dto);

@Transactional
void deleteClient(UUID id);
```

### Méthodes Métier
```java
// Recherche par nom
@Transactional(readOnly = true)
List<ClientResponseDto> searchClientsByName(String name);

// Vérifier si le client a des commandes en cours
@Transactional(readOnly = true)
boolean hasActiveOrders(UUID clientId);
```

---

## 5️⃣ UserService.java

**Localisation**: `src/main/java/com/example/stockgestion/services/UserService.java`

### Méthodes CRUD de Base
```java
// US1 - Créer un compte utilisateur
@Transactional
UserResponseDto createUser(UserRequestDto dto);

@Transactional(readOnly = true)
UserResponseDto getUserById(UUID id);

@Transactional(readOnly = true)
UserResponseDto getUserByEmail(String email);

@Transactional(readOnly = true)
List<UserResponseDto> getAllUsers();

@Transactional
UserResponseDto updateUser(UUID id, UserRequestDto dto);

@Transactional
void deleteUser(UUID id);
```

### Méthodes Métier (Rôles et Activation)
```java
// US1 - Activer un utilisateur
@Transactional
UserResponseDto activateUser(UUID id);

// US1 - Désactiver un utilisateur
@Transactional
UserResponseDto deactivateUser(UUID id);

// Vérifier si l'email existe déjà
@Transactional(readOnly = true)
boolean emailExists(String email);

// Récupérer les utilisateurs par rôle
@Transactional(readOnly = true)
List<UserResponseDto> getUsersByRole(Role role);

// Associer un utilisateur à un client (pour rôle CLIENT)
@Transactional
UserResponseDto linkUserToClient(UUID userId, UUID clientId);
```

---

## 6️⃣ SupplierService.java

**Localisation**: `src/main/java/com/example/stockgestion/services/SupplierService.java`

### Méthodes CRUD de Base
```java
// US14 - Gérer les fournisseurs
@Transactional
SupplierResponseDto createSupplier(SupplierRequestDto dto);

@Transactional(readOnly = true)
SupplierResponseDto getSupplierById(UUID id);

@Transactional(readOnly = true)
List<SupplierResponseDto> getAllSuppliers();

@Transactional
SupplierResponseDto updateSupplier(UUID id, SupplierRequestDto dto);

@Transactional
void deleteSupplier(UUID id);
```

### Méthodes Métier
```java
// Recherche par nom
@Transactional(readOnly = true)
List<SupplierResponseDto> searchSuppliersByName(String name);

// Vérifier si le fournisseur a des PO actifs
@Transactional(readOnly = true)
boolean hasActivePurchaseOrders(UUID supplierId);
```

---

## 7️⃣ CarrierService.java

**Localisation**: `src/main/java/com/example/stockgestion/services/CarrierService.java`

### Méthodes CRUD de Base
```java
@Transactional
CarrierResponseDto createCarrier(CarrierRequestDto dto);

@Transactional(readOnly = true)
CarrierResponseDto getCarrierById(UUID id);

@Transactional(readOnly = true)
List<CarrierResponseDto> getAllCarriers();

@Transactional
CarrierResponseDto updateCarrier(UUID id, CarrierRequestDto dto);

@Transactional
void deleteCarrier(UUID id);
```

### Méthodes Métier
```java
// Recherche par nom
@Transactional(readOnly = true)
List<CarrierResponseDto> searchCarriersByName(String name);
```

---

## 8️⃣ SalesOrderService.java

**Localisation**: `src/main/java/com/example/stockgestion/services/SalesOrderService.java`

### Méthodes CRUD de Base
```java
// ✅ Déjà implémenté partiellement
@Transactional
SalesOrderResponseDto createSalesOrder(SalesOrderRequestDto dto);

@Transactional(readOnly = true)
SalesOrderResponseDto getSalesOrderById(UUID orderId);

@Transactional(readOnly = true)
List<SalesOrderResponseDto> getAllSalesOrders();
```

### Méthodes Métier CRITIQUES - Cycle de Vie de la Commande
```java
// US4, US9 - Réserver le stock d'une commande (CREATED → RESERVED)
@Transactional
SalesOrderResponseDto reserveOrder(UUID orderId);

// US15 - Annuler une commande et libérer les réservations
@Transactional
SalesOrderResponseDto cancelOrder(UUID orderId);

// US11 - Marquer la commande comme EXPÉDIÉE (RESERVED → SHIPPED)
// Génère les OUTBOUND et libère les qtyReserved
@Transactional
SalesOrderResponseDto shipOrder(UUID orderId, UUID shipmentId);

// US11 - Marquer la commande comme LIVRÉE (SHIPPED → DELIVERED)
@Transactional
SalesOrderResponseDto markAsDelivered(UUID orderId);

// US4 - Créer automatiquement des backorders pour les quantités non disponibles
@Transactional
void createBackorder(UUID orderId, UUID productId, long missingQuantity);

// US5 - Récupérer les commandes d'un client
@Transactional(readOnly = true)
List<SalesOrderResponseDto> getOrdersByClientId(UUID clientId);

// Récupérer les commandes par statut
@Transactional(readOnly = true)
List<SalesOrderResponseDto> getOrdersByStatus(SOStatus status);

// Pagination
@Transactional(readOnly = true)
Page<SalesOrderResponseDto> getAllSalesOrders(Pageable pageable);

// Filtrage par date
@Transactional(readOnly = true)
List<SalesOrderResponseDto> getOrdersCreatedBetween(Instant from, Instant to);
```

### Méthodes Métier - Règles Avancées
```java
// RÈGLE: Cut-off 15h - Vérifier si la commande doit être planifiée pour le jour suivant
@Transactional(readOnly = true)
boolean isAfterCutoff(Instant orderTime);

// RÈGLE: Calculer la date de départ prévue (en tenant compte du cut-off)
@Transactional(readOnly = true)
Instant calculatePlannedDepartureDate(Instant orderTime);

// RÈGLE: TTL Réservation - Libérer les réservations expirées (24h)
@Transactional
void releaseExpiredReservations();
```

---

## 9️⃣ PurchaseOrderService.java

**Localisation**: `src/main/java/com/example/stockgestion/services/PurchaseOrderService.java`

### Méthodes CRUD de Base
```java
// US14 - Créer un Purchase Order
@Transactional
PurchaseOrderResponseDto createPurchaseOrder(PurchaseOrderRequestDto dto);

@Transactional(readOnly = true)
PurchaseOrderResponseDto getPurchaseOrderById(UUID id);

@Transactional(readOnly = true)
List<PurchaseOrderResponseDto> getAllPurchaseOrders();

@Transactional
PurchaseOrderResponseDto updatePurchaseOrder(UUID id, PurchaseOrderRequestDto dto);

@Transactional
void deletePurchaseOrder(UUID id);
```

### Méthodes Métier CRITIQUES - Cycle de Vie du PO
```java
// US14 - Approuver un PO (DRAFT → APPROVED)
@Transactional
PurchaseOrderResponseDto approvePurchaseOrder(UUID id);

// US14 - Réceptionner TOTALEMENT un PO (APPROVED → RECEIVED)
// Génère les INBOUND pour toutes les lignes
@Transactional
PurchaseOrderResponseDto receivePurchaseOrder(UUID id, UUID warehouseId);

// US14 - Réceptionner PARTIELLEMENT un PO
// Génère les INBOUND pour les quantités reçues seulement
@Transactional
PurchaseOrderResponseDto receivePartialPurchaseOrder(UUID id, UUID warehouseId, Map<UUID, Long> receivedQuantities);

// Annuler un PO (seulement si DRAFT ou APPROVED)
@Transactional
PurchaseOrderResponseDto cancelPurchaseOrder(UUID id);

// Récupérer les PO par fournisseur
@Transactional(readOnly = true)
List<PurchaseOrderResponseDto> getPurchaseOrdersBySupplierId(UUID supplierId);

// Récupérer les PO par statut
@Transactional(readOnly = true)
List<PurchaseOrderResponseDto> getPurchaseOrdersByStatus(POStatus status);

// Pagination
@Transactional(readOnly = true)
Page<PurchaseOrderResponseDto> getAllPurchaseOrders(Pageable pageable);
```

---

## 🔟 ShipmentService.java

**Localisation**: `src/main/java/com/example/stockgestion/services/ShipmentService.java`

### Méthodes CRUD de Base
```java
// US10 - Créer une expédition pour une commande RESERVED
@Transactional
ShipmentResponseDto createShipment(ShipmentRequestDto dto);

@Transactional(readOnly = true)
ShipmentResponseDto getShipmentById(UUID id);

@Transactional(readOnly = true)
List<ShipmentResponseDto> getAllShipments();

@Transactional
ShipmentResponseDto updateShipment(UUID id, ShipmentRequestDto dto);
```

### Méthodes Métier CRITIQUES - Cycle de Vie de l'Expédition
```java
// US10 - Planifier une expédition (statut PLANNED)
// Vérifie le cut-off et la capacité du créneau
@Transactional
ShipmentResponseDto planShipment(UUID salesOrderId, UUID carrierId, Instant plannedDate);

// US11 - Marquer comme EN TRANSIT (PLANNED → IN_TRANSIT)
@Transactional
ShipmentResponseDto markAsInTransit(UUID shipmentId);

// US11 - Marquer comme LIVRÉE (IN_TRANSIT → DELIVERED)
@Transactional
ShipmentResponseDto markAsDelivered(UUID shipmentId);

// US5 - Récupérer l'expédition d'une commande
@Transactional(readOnly = true)
List<ShipmentResponseDto> getShipmentsBySalesOrderId(UUID salesOrderId);

// Récupérer les expéditions par transporteur
@Transactional(readOnly = true)
List<ShipmentResponseDto> getShipmentsByCarrierId(UUID carrierId);

// Récupérer les expéditions par statut
@Transactional(readOnly = true)
List<ShipmentResponseDto> getShipmentsByStatus(ShipmentStatus status);

// Pagination
@Transactional(readOnly = true)
Page<ShipmentResponseDto> getAllShipments(Pageable pageable);
```

### Méthodes Métier - Règles Avancées
```java
// RÈGLE: Générer un numéro de suivi unique
@Transactional(readOnly = true)
String generateTrackingNumber();

// RÈGLE: Vérifier si le cut-off est dépassé (15h)
@Transactional(readOnly = true)
boolean isAfterCutoff(Instant shipmentTime);

// RÈGLE: Vérifier la capacité maximale du créneau
@Transactional(readOnly = true)
boolean hasAvailableSlotCapacity(Instant plannedDate);

// RÈGLE: Calculer le prochain créneau disponible
@Transactional(readOnly = true)
Instant getNextAvailableSlot(Instant requestedDate);
```

---

## 1️⃣1️⃣ ReportingService.java

**Localisation**: `src/main/java/com/example/stockgestion/services/ReportingService.java`

### Méthodes de Reporting et Statistiques
```java
// Nombre total de commandes par statut
@Transactional(readOnly = true)
Map<SOStatus, Long> getOrderCountByStatus();

// Taux de livraison (DELIVERED / TOTAL)
@Transactional(readOnly = true)
double getDeliveryRate();

// Détection des ruptures de stock (available <= 0)
@Transactional(readOnly = true)
List<InventoryResponseDto> getStockOutProducts();

// Détection des produits en faible stock (available < seuil)
@Transactional(readOnly = true)
List<InventoryResponseDto> getLowStockProducts(long threshold);

// Mouvements de stock filtrés par période
@Transactional(readOnly = true)
List<InventoryMovementResponseDto> getMovementsByDateRange(Instant from, Instant to);

// Nombre de commandes créées par période
@Transactional(readOnly = true)
long getOrderCountByDateRange(Instant from, Instant to);

// Statistiques des expéditions
@Transactional(readOnly = true)
Map<ShipmentStatus, Long> getShipmentCountByStatus();

// Top produits les plus commandés
@Transactional(readOnly = true)
List<ProductStatsDto> getTopOrderedProducts(int limit);

// Résumé d'inventaire global
@Transactional(readOnly = true)
InventorySummaryDto getInventorySummary();
```

---

## 📦 DTOs Additionnels à Créer

### InventoryAllocationDto.java
```java
public class InventoryAllocationDto {
    private UUID warehouseId;
    private String warehouseName;
    private long allocatedQuantity;
    private long availableStock;
}
```

### ProductStatsDto.java
```java
public class ProductStatsDto {
    private UUID productId;
    private String sku;
    private String name;
    private long totalOrdered;
}
```

### InventorySummaryDto.java
```java
public class InventorySummaryDto {
    private long totalProducts;
    private long totalQtyOnHand;
    private long totalQtyReserved;
    private long totalAvailable;
    private long productsOutOfStock;
}
```

---

## ⚙️ Services Helpers (déjà présents)

### StockReservationHelper.java
```java
// Réserver le stock sur plusieurs entrepôts
ReservationResult reserveAcrossWarehouses(
    SalesOrder order, 
    Product product, 
    BigDecimal unitPrice, 
    long requestedQty, 
    List<Inventory> inventories, 
    List<Inventory> inventoriesToUpdate
);

// Récupérer les inventaires triés par disponibilité
List<Inventory> getInventoriesSortedByAvailability(Product product);

// Sauvegarder les inventaires en batch
void saveInventoriesInBatch(List<Inventory> inventories);
```

### OrderStatusHelper.java
```java
// Déterminer le statut final de la commande
SOStatus determineStatus(boolean hasBackorder, boolean hasReserved);
```

### SalesOrderBuilder.java
```java
// Initialiser une nouvelle commande
SalesOrder initialize(Client client);

// Finaliser la commande avec lignes et total
void finalize(SalesOrder order, List<SalesOrderLine> lines, BigDecimal totalPrice, SOStatus status);
```

### ClientValidator.java
```java
// Valider et récupérer un client
Client validateAndGet(UUID clientId);
```

### ProductValidator.java
```java
// Valider et récupérer un produit
Product validateAndGet(UUID productId);

// Valider et récupérer le prix
BigDecimal validateAndGetPrice(Product product);
```

---

## 🔐 Configuration des Tâches Planifiées

### ScheduledTasksService.java
**Localisation**: `src/main/java/com/example/stockgestion/services/ScheduledTasksService.java`

```java
// Libérer les réservations expirées (toutes les heures)
@Scheduled(cron = "0 0 * * * *") // Toutes les heures
@Transactional
public void releaseExpiredReservations();
```

---

## 📝 Résumé des Priorités

### 🔴 CRITIQUE (à implémenter en priorité)
1. **InventoryService**: `reserveStock()`, `releaseReservation()`, `allocateStockAcrossWarehouses()`
2. **SalesOrderService**: `reserveOrder()`, `cancelOrder()`, `shipOrder()`, `markAsDelivered()`
3. **PurchaseOrderService**: `approvePurchaseOrder()`, `receivePurchaseOrder()`, `receivePartialPurchaseOrder()`
4. **ShipmentService**: `planShipment()`, `markAsInTransit()`, `markAsDelivered()`

### 🟡 IMPORTANT (règles avancées)
5. Cut-off 15h dans `SalesOrderService` et `ShipmentService`
6. TTL réservation 24h avec `ScheduledTasksService`
7. Capacité maximale créneaux dans `ShipmentService`
8. Génération backorders automatiques

### 🟢 SECONDAIRE (reporting et amélioration)
9. `ReportingService` complet
10. Pagination sur tous les services
11. Filtres avancés

---

## 🎯 Ordre d'Implémentation Recommandé

1. **ProductService** → Compléter avec activate/deactivate
2. **WarehouseService** → Compléter avec validation suppression
3. **InventoryService** → PRIORITÉ ABSOLUE (reserve/release/allocate)
4. **ClientService** → CRUD complet
5. **UserService** → CRUD complet
6. **SupplierService** → CRUD complet
7. **CarrierService** → CRUD complet
8. **PurchaseOrderService** → Cycle de vie complet
9. **SalesOrderService** → Compléter le cycle de vie
10. **ShipmentService** → Cycle de vie complet + règles avancées
11. **ReportingService** → Statistiques
12. **ScheduledTasksService** → Tâches planifiées

---

**Note**: Tous les services utilisent **ModelMapper** pour les conversions entre entités et DTOs, conformément à votre architecture existante.
