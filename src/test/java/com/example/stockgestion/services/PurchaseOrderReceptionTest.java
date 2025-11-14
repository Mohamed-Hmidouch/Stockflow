package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.ReceivedLineDto;
import com.example.stockgestion.Dto.request.ReceiveRequestDto;
import com.example.stockgestion.Dto.response.PurchaseOrderResponseDto;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.*;
import com.example.stockgestion.models.enums.MovementType;
import com.example.stockgestion.models.enums.POStatus;
import com.example.stockgestion.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderReceptionTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderLineRepository purchaseOrderLineRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WareHouseRepository wareHouseRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMovmentRepository inventoryMovmentRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private UUID orderId;
    private UUID warehouseId;
    private UUID productId;
    private PurchaseOrder purchaseOrder;
    private WareHouse warehouse;
    private Product product;
    private PurchaseOrderLine line;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
        productId = UUID.randomUUID();

        warehouse = new WareHouse();
        warehouse.setId(warehouseId);

        product = new Product();
        product.setId(productId);

        line = new PurchaseOrderLine();
        line.setProduct(product);
        line.setQuantity(100);
        line.setQtyReceived(0);

        purchaseOrder = new PurchaseOrder();
        purchaseOrder.setId(orderId);
        purchaseOrder.setStatus(POStatus.APPROVED);
        purchaseOrder.setLines(Arrays.asList(line));

        inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQtyOnHand(50);
        inventory.setQtyReserved(0);
    }

    @Test
    void receptionOrder_FullReceipt_ShouldCompleteOrder() {
        // Given
        ReceivedLineDto receivedLineDto = new ReceivedLineDto();
        receivedLineDto.setProductId(productId);
        receivedLineDto.setPoLine(line.getId());
        receivedLineDto.setQuantityReceived(100);

        ReceiveRequestDto requestDto = new ReceiveRequestDto();
        requestDto.setWarehouseId(warehouseId);
        requestDto.setReceivedLineDto(Arrays.asList(receivedLineDto));

        when(purchaseOrderRepository.findById(orderId)).thenReturn(Optional.of(purchaseOrder));
        when(wareHouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(Arrays.asList(inventory));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);

        PurchaseOrderResponseDto responseDto = new PurchaseOrderResponseDto();
        when(modelMapper.map(any(PurchaseOrder.class), eq(PurchaseOrderResponseDto.class)))
                .thenReturn(responseDto);

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.receptionOrder(orderId, requestDto);

        // Then
        assertNotNull(result);
        verify(purchaseOrderLineRepository).saveAll(anyList());
        verify(inventoryRepository).saveAll(anyList());
        verify(inventoryMovmentRepository).saveAll(anyList());
        verify(eventPublisher, atLeastOnce()).publishEvent(any(Object.class));
    }

    @Test
    void receptionOrder_PartialReceipt_ShouldStayApproved() {
        // Given
        ReceivedLineDto receivedLineDto = new ReceivedLineDto();
        receivedLineDto.setProductId(productId);
        receivedLineDto.setPoLine(line.getId());
        receivedLineDto.setQuantityReceived(50);

        ReceiveRequestDto requestDto = new ReceiveRequestDto();
        requestDto.setWarehouseId(warehouseId);
        requestDto.setReceivedLineDto(Arrays.asList(receivedLineDto));

        when(purchaseOrderRepository.findById(orderId)).thenReturn(Optional.of(purchaseOrder));
        when(wareHouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(Arrays.asList(inventory));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);

        PurchaseOrderResponseDto responseDto = new PurchaseOrderResponseDto();
        when(modelMapper.map(any(PurchaseOrder.class), eq(PurchaseOrderResponseDto.class)))
                .thenReturn(responseDto);

        // When
        purchaseOrderService.receptionOrder(orderId, requestDto);

        // Then
        verify(inventoryRepository).saveAll(anyList());
        verify(inventoryMovmentRepository).saveAll(anyList());
    }

    @Test
    void receptionOrder_CreateNewInventory_WhenNotExists() {
        // Given
        ReceivedLineDto receivedLineDto = new ReceivedLineDto();
        receivedLineDto.setProductId(productId);
        receivedLineDto.setPoLine(line.getId());
        receivedLineDto.setQuantityReceived(100);

        ReceiveRequestDto requestDto = new ReceiveRequestDto();
        requestDto.setWarehouseId(warehouseId);
        requestDto.setReceivedLineDto(Arrays.asList(receivedLineDto));

        when(purchaseOrderRepository.findById(orderId)).thenReturn(Optional.of(purchaseOrder));
        when(wareHouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(Collections.emptyList());
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);

        PurchaseOrderResponseDto responseDto = new PurchaseOrderResponseDto();
        when(modelMapper.map(any(PurchaseOrder.class), eq(PurchaseOrderResponseDto.class)))
                .thenReturn(responseDto);

        // When
        purchaseOrderService.receptionOrder(orderId, requestDto);

        // Then
        verify(inventoryRepository).saveAll(argThat(list -> {
            List<Inventory> inventories = (List<Inventory>) list;
            return inventories.size() > 0 && inventories.get(0).getQtyOnHand() == 100;
        }));
    }

    @Test
    void receptionOrder_WhenProductNotFound_ShouldThrowException() {
        // Given
        ReceivedLineDto receivedLineDto = new ReceivedLineDto();
        receivedLineDto.setProductId(productId);
        receivedLineDto.setPoLine(line.getId());
        receivedLineDto.setQuantityReceived(50);

        ReceiveRequestDto requestDto = new ReceiveRequestDto();
        requestDto.setWarehouseId(warehouseId);
        requestDto.setReceivedLineDto(Arrays.asList(receivedLineDto));

        when(purchaseOrderRepository.findById(orderId)).thenReturn(Optional.of(purchaseOrder));
        when(wareHouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                purchaseOrderService.receptionOrder(orderId, requestDto));
    }

    @Test
    void receptionOrder_WhenLineNotFound_ShouldThrowException() {
        // Given
        UUID wrongProductId = UUID.randomUUID();
        ReceivedLineDto receivedLineDto = new ReceivedLineDto();
        receivedLineDto.setProductId(wrongProductId);
        receivedLineDto.setPoLine(UUID.randomUUID());
        receivedLineDto.setQuantityReceived(50);

        ReceiveRequestDto requestDto = new ReceiveRequestDto();
        requestDto.setWarehouseId(warehouseId);
        requestDto.setReceivedLineDto(Arrays.asList(receivedLineDto));

        Product wrongProduct = new Product();
        wrongProduct.setId(wrongProductId);

        when(purchaseOrderRepository.findById(orderId)).thenReturn(Optional.of(purchaseOrder));
        when(wareHouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(wrongProductId)).thenReturn(Optional.of(wrongProduct));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(wrongProductId, warehouseId))
                .thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                purchaseOrderService.receptionOrder(orderId, requestDto));
    }

    @Test
    void receptionOrder_WhenAlreadyReceived_ShouldThrowException() {
        // Given
        purchaseOrder.setStatus(POStatus.RECEIVED);

        ReceiveRequestDto requestDto = new ReceiveRequestDto();
        requestDto.setWarehouseId(warehouseId);

        when(purchaseOrderRepository.findById(orderId)).thenReturn(Optional.of(purchaseOrder));

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                purchaseOrderService.receptionOrder(orderId, requestDto));
    }

    @Test
    void receptionOrder_WhenCanceled_ShouldThrowException() {
        // Given
        purchaseOrder.setStatus(POStatus.CANCELED);

        ReceiveRequestDto requestDto = new ReceiveRequestDto();
        requestDto.setWarehouseId(warehouseId);

        when(purchaseOrderRepository.findById(orderId)).thenReturn(Optional.of(purchaseOrder));

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                purchaseOrderService.receptionOrder(orderId, requestDto));
    }
}
