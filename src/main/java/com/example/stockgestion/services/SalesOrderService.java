package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.SalesOrderRequestDto;
import com.example.stockgestion.Dto.response.SalesOrderResponseDto;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.Client;
import com.example.stockgestion.models.Inventory;
import com.example.stockgestion.models.Product;
import com.example.stockgestion.models.SalesOrder;
import com.example.stockgestion.models.SalesOrderLine;
import com.example.stockgestion.models.enums.SOStatus;
import com.example.stockgestion.repositories.SalesOrderRepository;
import com.example.stockgestion.services.helpers.*;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import java.lang.reflect.Type;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SalesOrderService {
    private final SalesOrderRepository salesOrderRepository;
    private final ModelMapper modelMapper;
    
    // Helpers
    private final ClientValidator clientValidator;
    private final ProductValidator productValidator;
    private final StockReservationHelper stockReservationHelper;
    private final OrderStatusHelper orderStatusHelper;
    private final SalesOrderBuilder salesOrderBuilder;

    @Transactional
    public SalesOrderResponseDto createSalesOrder(SalesOrderRequestDto dto) {
        Client client = clientValidator.validateAndGet(dto.getClientId());
        SalesOrder order = salesOrderBuilder.initialize(client);
        
        List<SalesOrderLine> lines = new ArrayList<>();
        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        boolean[] hasBackorder = {false};
        boolean[] hasReserved = {false};

        BigDecimal totalPrice = dto.getLines().stream()
            .map(lineDto -> {
                Product product = productValidator.validateAndGet(lineDto.getProductId());
                BigDecimal unitPrice = productValidator.validateAndGetPrice(product);
                long requestedQty = lineDto.getQuantity();
                
                List<Inventory> inventories = stockReservationHelper.getInventoriesSortedByAvailability(product);
                
                ReservationResult result = stockReservationHelper.reserveAcrossWarehouses(
                    order, product, unitPrice, requestedQty, inventories, inventoriesToUpdate
                );
                
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
    List<SalesOrderResponseDto> getAllSalesOrders(){
        List<SalesOrder> orders = salesOrderRepository.findAll();
        Type listType = new TypeToken<List<SalesOrderResponseDto>>() {}.getType();
        return modelMapper.map(orders, listType);
    }
}
