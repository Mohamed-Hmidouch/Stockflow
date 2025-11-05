package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.SalesOrderRequestDto;
import com.example.stockgestion.Dto.response.SalesOrderResponseDto;
import com.example.stockgestion.events.StockReceivedEvent;
import com.example.stockgestion.exception.BusinessRuleException;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.*;
import com.example.stockgestion.models.enums.MovementType;
import com.example.stockgestion.models.enums.SOStatus;
import com.example.stockgestion.repositories.InventoryMovmentRepository;
import com.example.stockgestion.repositories.InventoryRepository;
import com.example.stockgestion.repositories.SalesOrderLineRepository;
import com.example.stockgestion.repositories.SalesOrderRepository;
import com.example.stockgestion.services.helpers.*;

import lombok.AllArgsConstructor;

import org.apache.tomcat.util.net.SocketEvent;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import java.lang.reflect.Type;

import org.springframework.context.event.EventListener;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SalesOrderService {
    private final SalesOrderRepository salesOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;

    // Helpers
    private final ClientValidator clientValidator;
    private final ProductValidator productValidator;
    private final StockReservationHelper stockReservationHelper;
    private final OrderStatusHelper orderStatusHelper;
    private final SalesOrderBuilder salesOrderBuilder;
    private final InventoryMovmentRepository inventoryMovmentRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;

    @Transactional
    public SalesOrderResponseDto createSalesOrder(SalesOrderRequestDto dto) {
        Client client = clientValidator.validateAndGet(dto.getClientId());
        SalesOrder order = salesOrderBuilder.initialize(client);

        List<SalesOrderLine> lines = new ArrayList<>();
        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        boolean[] hasBackorder = { false };
        boolean[] hasReserved = { false };

        BigDecimal totalPrice = dto.getLines().stream()
                .map(lineDto -> {
                    Product product = productValidator.validateAndGet(lineDto.getProductId());
                    BigDecimal unitPrice = productValidator.validateAndGetPrice(product);
                    long requestedQty = lineDto.getQuantity();

                    List<Inventory> inventories = stockReservationHelper.getInventoriesSortedByAvailability(product);

                    ReservationResult result = stockReservationHelper.reserveAcrossWarehouses(
                            order, product, unitPrice, requestedQty, inventories, inventoriesToUpdate);

                    lines.addAll(result.getLines());
                    hasReserved[0] = hasReserved[0] || result.isHasReserved();
                    hasBackorder[0] = hasBackorder[0] || result.isHasBackorder();

                    return unitPrice.multiply(BigDecimal.valueOf(requestedQty));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SOStatus finalStatus = orderStatusHelper.determineStatus(hasBackorder[0], hasReserved[0]);
        salesOrderBuilder.finalize(order, lines, totalPrice, finalStatus);
        stockReservationHelper.saveInventoriesInBatch(inventoriesToUpdate);

        SalesOrder savedOrder = salesOrderRepository.save(order);
        return modelMapper.map(savedOrder, SalesOrderResponseDto.class);
    }

    public SalesOrderResponseDto getSalesOrderById(UUID orderId) {
        SalesOrder salesOrder = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", orderId));
        return modelMapper.map(salesOrder, SalesOrderResponseDto.class);
    }

    @Transactional(readOnly = true)
    List<SalesOrderResponseDto> getAllSalesOrders() {
        List<SalesOrder> orders = salesOrderRepository.findAll();
        Type listType = new TypeToken<List<SalesOrderResponseDto>>() {
        }.getType();
        return modelMapper.map(orders, listType);
    }

    @Transactional
    public SalesOrderResponseDto cancelOrder(UUID orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", orderId));
        if (order.getStatus() == SOStatus.DELIVERED)
            throw new BusinessRuleException("Impossible d'annuler une commande déjà expédiée ou livrée.");
        if (order.getStatus() == SOStatus.CANCELED)
            return modelMapper.map(order, SalesOrderResponseDto.class);
        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        if (order.getStatus() == SOStatus.RESERVED || order.getStatus() == SOStatus.PARTIALLY_RESERVED) {
            order.getLines().forEach(line -> {
                if (line.getQtyReserved() > 0) {
                    List<Inventory> inventories = inventoryRepository
                            .findByProduct_IdAndWarehouse_Id(line.getProduct().getId(), line.getWarehouse().getId());
                    if (!inventories.isEmpty()) {
                        Inventory inventory = inventories.get(0);
                        inventory.setQtyReserved(inventory.getQtyReserved() - line.getQtyReserved());
                        inventoriesToUpdate.add(inventory);
                    } else {
                        throw new ResourceNotFoundException("Inventory", "product/warehouse",
                                line.getProduct().getId());
                    }
                }
            });
        }
        if (!inventoriesToUpdate.isEmpty())
            inventoryRepository.saveAll(inventoriesToUpdate);
        order.setStatus(SOStatus.CANCELED);
        SalesOrder savedOrder = salesOrderRepository.save(order);
        return modelMapper.map(savedOrder, SalesOrderResponseDto.class);
    }

    @Transactional
    public SalesOrderResponseDto shipOrder(UUID orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", orderId));
        if (order.getStatus() != SOStatus.RESERVED && order.getStatus() != SOStatus.PARTIALLY_RESERVED)
            throw new BusinessRuleException("Impossible d'epédier une commande qui n'est pas reserver !!!!");
        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        List<InventoryMovement> movementsToCreate = new ArrayList<>();
        if (order.getStatus() == SOStatus.RESERVED || order.getStatus() == SOStatus.PARTIALLY_RESERVED) {
            order.getLines().forEach(line -> {
                if (line.getQtyReserved() > 0) {
                    List<Inventory> inventories = inventoryRepository
                            .findByProduct_IdAndWarehouse_Id(line.getProduct().getId(), line.getWarehouse().getId());
                    if (!inventories.isEmpty()) {
                        Inventory inventory = inventories.get(0);
                        long qtyToShip = line.getQtyReserved();
                        inventory.setQtyOnHand(inventory.getQtyOnHand() - qtyToShip);
                        inventory.setQtyReserved(inventory.getQtyReserved() - qtyToShip);
                        inventoriesToUpdate.add(inventory);

                        InventoryMovement inventoryMovement = new InventoryMovement();
                        inventoryMovement.setProduct(line.getProduct());
                        inventoryMovement.setWarehouse(line.getWarehouse());
                        inventoryMovement.setQuantity(-qtyToShip); // B "moins" 7it "Sortie"
                        inventoryMovement.setType(MovementType.OUTBOUND); // Khas n7eddo Type
                        inventoryMovement.setOccurredAt(Instant.now()); // W l-weqt
                        movementsToCreate.add(inventoryMovement);
                    } else {
                        throw new ResourceNotFoundException("Inventory", "product/warehouse",
                                line.getProduct().getId());
                    }
                }
            });
            if (!movementsToCreate.isEmpty())
                inventoryMovmentRepository.saveAll(movementsToCreate);
            if (!inventoriesToUpdate.isEmpty())
                inventoryRepository.saveAll(inventoriesToUpdate);
            order.setStatus(SOStatus.DELIVERED);
            SalesOrder savedOrder = salesOrderRepository.save(order);
            return modelMapper.map(savedOrder, SalesOrderResponseDto.class);
        }
        return modelMapper.map(order, SalesOrderResponseDto.class);
    }

    @EventListener
    public void handleStockReceived(StockReceivedEvent event) {
        List<Inventory> inventorys = inventoryRepository.findByProduct_IdAndWarehouse_Id(event.productId(),
                event.WarehouseId());
        if (inventorys.isEmpty()) {
            return;
        }
        Inventory inventory = inventorys.get(0);
        long initialQtyDispo = inventory.getQtyOnHand() - inventory.getQtyReserved();
        long qtyDispo = initialQtyDispo;
        if (qtyDispo <= 0)
            return;
        List<SalesOrderLine> linesToSendStock = salesOrderLineRepository
                .findByProductIdAndQtyBackorderedGreaterThanOrderByCreatedAtAsc(event.productId(), 0);

        List<SalesOrderLine> linesToUpdate = new ArrayList<>();
        List<SalesOrder> ordersToUpdateStatus = new ArrayList<>();

        for (SalesOrderLine line : linesToSendStock) {
            long qtyToSend = Math.min(line.getQtyBackordered(), qtyDispo);
            line.setQtyBackordered(line.getQtyBackordered() - qtyToSend);
            line.setQtyReserved(line.getQtyReserved() + qtyToSend);
            linesToUpdate.add(line);
            qtyDispo = qtyDispo - qtyToSend;
            ordersToUpdateStatus.add(line.getSalesOrder());
            if (qtyDispo <= 0)
                break;
        }
        long totalQtyAllocated = initialQtyDispo - qtyDispo;
        inventory.setQtyReserved(inventory.getQtyReserved() + totalQtyAllocated);
        inventoryRepository.save(inventory);
        if (!linesToUpdate.isEmpty())
            salesOrderLineRepository.saveAll(linesToUpdate);
        Set<SalesOrder> ordersTocheck = new HashSet<>(ordersToUpdateStatus);
        List<SalesOrder> orderToSave = new ArrayList<>();
        for (SalesOrder order : ordersTocheck) {
            boolean isStilPartial = order.getLines().stream().anyMatch(l -> l.getQtyBackordered() > 0);
            if (!isStilPartial) {
                order.setStatus(SOStatus.RESERVED);
                orderToSave.add(order);
            }
        }
        if (!orderToSave.isEmpty())
            salesOrderRepository.saveAll(orderToSave);
    }
}