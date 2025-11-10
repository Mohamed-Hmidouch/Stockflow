# Scénarios d'Implémentation pour InventoryService

## 5 Scénarios Simples (Pour démarrer)

### S1: Réception complète d'un PurchaseOrder (INBOUND)
**Contexte:** Un PurchaseOrder avec statut `APPROVED` est reçu complètement.  
**Action:** Pour chaque ligne du PurchaseOrder, augmenter `qtyOnHand` et créer un `InventoryMovement` de type `INBOUND`.  
**Résultat attendu:**  
- `qtyOnHand` augmenté de la quantité reçue
- Mouvement `INBOUND` créé avec `referenceDoc = purchaseOrderId`
- Statut du PurchaseOrder mis à `RECEIVED`
- `qtyReceived` dans chaque `PurchaseOrderLine` mis à jour

**Méthode à créer:** `receivePurchaseOrder(UUID purchaseOrderId)`

---

### S2: Réserver le stock pour une SalesOrder (RESERVATION)
**Contexte:** Une SalesOrder avec statut `CREATED` doit être réservée.  
**Action:** Pour chaque ligne de la commande, vérifier la disponibilité (`qtyOnHand - qtyReserved`) et réserver.  
**Résultat attendu:**  
- `qtyReserved` augmenté pour chaque produit/entrepôt
- Statut de la SalesOrder mis à `RESERVED`
- Si stock insuffisant: `BusinessRuleException`

**Méthode à créer:** Déjà implémentée (`reserve()`)

---

### S3: Expédier une SalesOrder (OUTBOUND)
**Contexte:** Une SalesOrder avec statut `RESERVED` est expédiée.  
**Action:** Pour chaque ligne, diminuer `qtyOnHand` et `qtyReserved`, créer un mouvement `OUTBOUND`.  
**Résultat attendu:**  
- `qtyOnHand` et `qtyReserved` diminués
- Mouvement `OUTBOUND` créé
- Statut de la SalesOrder mis à `SHIPPED`

**Méthode à créer:** Déjà implémentée (`outbound()`)

---

### S4: Ajuster le stock manuellement (ADJUSTMENT)
**Contexte:** Un inventaire physique révèle un écart (produit cassé, comptage erroné).  
**Action:** Créer un ajustement positif ou négatif selon l'écart.  
**Résultat attendu:**  
- Si négatif: vérifier que `qtyOnHand - qtyReserved >= |quantity|`
- `qtyOnHand` ajusté
- Mouvement `ADJUSTMENT` créé avec raison

**Méthode à créer:** Déjà implémentée (`adjust()`)

---

### S5: Consulter le stock disponible par produit et entrepôt
**Contexte:** Un utilisateur veut connaître le stock disponible (non réservé) pour un produit dans un entrepôt.  
**Action:** Calculer `dispo = qtyOnHand - qtyReserved` pour un produit/entrepôt donné.  
**Résultat attendu:**  
- Retourner un DTO avec `qtyOnHand`, `qtyReserved`, `available` (calculé)

**Méthode à créer:** `getAvailableStock(UUID productId, UUID warehouseId)`

---

## 5 Scénarios Moyens (Intégration avec autres modules)

### M1: Réception partielle d'un PurchaseOrder
**Contexte:** Un PurchaseOrder est reçu partiellement (ex: 50 sur 100 unités reçues).  
**Action:** Mettre à jour seulement les lignes reçues, créer les mouvements correspondants.  
**Résultat attendu:**  
- `qtyReceived` mis à jour dans `PurchaseOrderLine`
- `qtyOnHand` augmenté uniquement pour les quantités reçues
- Mouvements `INBOUND` créés
- Statut du PurchaseOrder mis à `PARTIALLY_RECEIVED` si partiel, `RECEIVED` si complet
- Ne pas dépasser la quantité commandée

**Méthode à créer:** `receivePurchaseOrderPartially(UUID purchaseOrderId, Map<UUID, Long> lineIdToReceivedQty)`

---

### M2: Annuler une réservation et libérer le stock
**Contexte:** Une SalesOrder réservée est annulée (statut `CANCELED`).  
**Action:** Libérer les réservations (diminuer `qtyReserved`) sans toucher à `qtyOnHand`.  
**Résultat attendu:**  
- `qtyReserved` diminué pour chaque ligne
- Pas de mouvement créé (le stock n'a pas bougé physiquement)
- Statut de la SalesOrder mis à `CANCELED`

**Méthode à créer:** `cancelReservation(UUID salesOrderId)` ou `releaseReservation()` (déjà implémentée)

---

### M3: Transfert de stock entre entrepôts avec traçabilité
**Contexte:** Déplacer 20 unités d'un produit de l'entrepôt A vers l'entrepôt B.  
**Action:** Créer deux mouvements (OUTBOUND depuis A, INBOUND vers B) et mettre à jour les inventaires.  
**Résultat attendu:**  
- `qtyOnHand` diminué dans l'entrepôt source
- `qtyOnHand` augmenté dans l'entrepôt destination
- Vérifier disponibilité avant transfert
- Deux mouvements créés avec même `referenceDoc` (ex: "TRF-2025-001")
- Si stock insuffisant: `BusinessRuleException`

**Méthode à créer:** Déjà implémentée (`transfer()`)

---

### M4: Obtenir le stock total d'un produit sur tous les entrepôts
**Contexte:** Un utilisateur veut connaître le stock total (tous entrepôts confondus) d'un produit.  
**Action:** Agréger `qtyOnHand` et `qtyReserved` pour tous les entrepôts.  
**Résultat attendu:**  
- DTO avec `totalQtyOnHand`, `totalQtyReserved`, `totalAvailable`
- Liste des inventaires par entrepôt (optionnel)

**Méthode à créer:** `getTotalStockByProduct(UUID productId)` (partiellement implémentée avec summary)

---

### M5: Créer automatiquement un Inventory lors de la première réception
**Contexte:** Un produit n'a jamais été reçu dans un entrepôt (pas d'Inventory existant).  
**Action:** Lors d'un INBOUND, créer automatiquement l'Inventory si elle n'existe pas.  
**Résultat attendu:**  
- Inventory créé avec `qtyOnHand = quantity`, `qtyReserved = 0`
- Mouvement `INBOUND` créé
- Pas d'erreur si l'Inventory n'existe pas

**Méthode à créer:** Déjà géré dans `createMovement()` et `adjust()`

---

## 5 Scénarios Complexes (Logique métier avancée)

### C1: Réception d'un PurchaseOrder avec backorder (réception partielle + suivi)
**Contexte:** Un PurchaseOrder de 100 unités est partiellement reçu (70 reçues, 30 en attente).  
**Action:** Gérer la réception partielle et suivre les quantités manquantes.  
**Résultat attendu:**  
- `qtyReceived` mis à jour (70)
- `qtyOnHand` augmenté de 70
- Mouvements `INBOUND` créés pour 70 unités
- Statut `PARTIALLY_RECEIVED`
- Possibilité de compléter la réception plus tard (les 30 restantes)
- Vérifier que `qtyReceived <= quantity` dans `PurchaseOrderLine`

**Méthode à créer:** `receivePurchaseOrderPartially()` avec validation des quantités

---

### C2: Réservation partielle avec allocation multi-entrepôts
**Contexte:** Une SalesOrder nécessite 100 unités, mais aucun entrepôt n'a assez de stock seul.  
**Action:** Allouer le stock depuis plusieurs entrepôts (ex: 60 depuis Paris, 40 depuis Lyon).  
**Résultat attendu:**  
- Vérifier disponibilité totale (tous entrepôts confondus)
- Si suffisant: réserver depuis plusieurs entrepôts
- Créer des réservations multiples (une par entrepôt)
- Si insuffisant: `BusinessRuleException` avec détails

**Méthode à créer:** `reserveFromMultipleWarehouses(UUID salesOrderId, Map<UUID, UUID> lineIdToWarehouseId)`

---

### C3: Expédition avec gestion des Shipments et tracking
**Contexte:** Une SalesOrder `RESERVED` est expédiée avec création d'un Shipment et numéro de suivi.  
**Action:** Créer le Shipment, mettre à jour l'inventaire, lier les mouvements au Shipment.  
**Résultat attendu:**  
- Shipment créé avec statut `PLANNED` ou `DELIVERED`
- `qtyOnHand` et `qtyReserved` diminués
- Mouvements `OUTBOUND` créés avec `referenceDoc = shipmentId` ou `salesOrderId`
- Statut SalesOrder mis à `SHIPPED`
- Tracking number enregistré

**Méthode à créer:** `shipSalesOrder(UUID salesOrderId, UUID carrierId, String trackingNumber)`

---

### C4: Ajustement de stock avec validation des réservations en cours
**Contexte:** Un ajustement négatif doit vérifier que les réservations en cours ne sont pas impactées.  
**Action:** Vérifier que `qtyOnHand - qtyReserved >= |adjustment|` avant d'appliquer l'ajustement.  
**Résultat attendu:**  
- Si ajustement négatif: vérifier disponibilité
- Si ajustement rend le stock disponible < réservé: `BusinessRuleException`
- Appliquer l'ajustement seulement si sûr
- Mouvement `ADJUSTMENT` créé avec raison détaillée

**Méthode à créer:** Déjà implémentée (`adjust()`) mais peut être améliorée avec validation plus stricte

---

### C5: Réception d'un PurchaseOrder avec validation des quantités et statuts
**Contexte:** Réceptionner un PurchaseOrder avec validation des statuts et quantités reçues.  
**Action:** Valider que le PurchaseOrder est `APPROVED`, vérifier que les quantités reçues ne dépassent pas les quantités commandées, mettre à jour les statuts.  
**Résultat attendu:**  
- Vérifier statut `APPROVED` ou `PARTIALLY_RECEIVED`
- Valider `qtyReceived <= quantity` pour chaque ligne
- Mettre à jour `qtyReceived` dans `PurchaseOrderLine`
- Créer les mouvements `INBOUND`
- Mettre à jour statut PurchaseOrder (`RECEIVED` si complet, `PARTIALLY_RECEIVED` si partiel)
- Si quantités invalides: `BusinessRuleException`

**Méthode à créer:** `receivePurchaseOrder(UUID purchaseOrderId, Map<UUID, Long> receivedQuantities)`

---

## Résumé des Méthodes à Implémenter

### Simples (S1-S5)
- [x] `reserve()` - Déjà implémentée
- [x] `outbound()` - Déjà implémentée
- [x] `adjust()` - Déjà implémentée
- [ ] `receivePurchaseOrder(UUID purchaseOrderId)` - À implémenter
- [ ] `getAvailableStock(UUID productId, UUID warehouseId)` - À implémenter

### Moyens (M1-M5)
- [x] `releaseReservation()` - Déjà implémentée
- [x] `transfer()` - Déjà implémentée
- [x] `getTotalStockByProduct()` - Partiellement implémentée (summary)
- [ ] `receivePurchaseOrderPartially(UUID purchaseOrderId, Map<UUID, Long> lineIdToReceivedQty)` - À implémenter
- [ ] `cancelReservation(UUID salesOrderId)` - À implémenter (ou utiliser releaseReservation)

### Complexes (C1-C5)
- [ ] `receivePurchaseOrderPartially()` avec validation - À implémenter
- [ ] `reserveFromMultipleWarehouses()` - À implémenter (optionnel, cas avancé)
- [ ] `shipSalesOrder(UUID salesOrderId, UUID carrierId, String trackingNumber)` - À implémenter
- [ ] `receivePurchaseOrder()` avec validation complète - À implémenter

---

## Notes d'Implémentation

1. **Transactions:** Toutes les méthodes modifiant l'inventaire doivent être `@Transactional`
2. **Exceptions:** Utiliser `BusinessRuleException` pour les règles métier, `ResourceNotFoundException` pour les entités non trouvées
3. **Validation:** Valider les statuts des commandes avant toute opération
4. **Traçabilité:** Toujours créer des `InventoryMovement` pour les opérations physiques
5. **Intégrité:** Vérifier les contraintes (stock disponible, quantités, statuts) avant toute modification

