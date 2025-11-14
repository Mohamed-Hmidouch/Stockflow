package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.InventoryMovementRequestDto;
import com.example.stockgestion.Dto.request.InventoryRequestDto;
import com.example.stockgestion.Dto.response.InventoryMovementResponseDto;
import com.example.stockgestion.Dto.response.InventoryResponseDto;
import com.example.stockgestion.exception.BusinessRuleException;
import com.example.stockgestion.exception.ConflictException;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.Inventory;
import com.example.stockgestion.models.InventoryMovement;
import com.example.stockgestion.models.Product;
import com.example.stockgestion.models.WareHouse;
import com.example.stockgestion.models.enums.MovementType;
import com.example.stockgestion.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WareHouseRepository wareHouseRepository;

    @Mock
    private InventoryMovmentRepository inventoryMovmentRepository;

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private UUID inventoryId;
    private UUID productId;
    private UUID warehouseId;
    private Product product;
    private WareHouse warehouse;
    private Inventory inventory;
    private InventoryRequestDto inventoryRequestDto;

    @BeforeEach
    void setUp() {
        inventoryId = UUID.randomUUID();
        productId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();

        product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setSku("TEST-SKU");
        product.setPrice(BigDecimal.valueOf(100));

        warehouse = new WareHouse();
        warehouse.setId(warehouseId);
        warehouse.setName("Test Warehouse");
        warehouse.setCode("WH-TEST");

        inventory = new Inventory();
        inventory.setId(inventoryId);
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQtyOnHand(100);
        inventory.setQtyReserved(10);

        inventoryRequestDto = new InventoryRequestDto();
        inventoryRequestDto.setProductId(productId);
        inventoryRequestDto.setWarehouseId(warehouseId);
        inventoryRequestDto.setQtyOnHand(100L);
        inventoryRequestDto.setQtyReserved(0L);
    }

    @Test
    void createInventory_ShouldSucceed_WhenValidData() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wareHouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.existsByProduct_IdAndWarehouse_Id(productId, warehouseId)).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // When
        InventoryResponseDto result = inventoryService.createInventory(inventoryRequestDto);

        // Then
        assertNotNull(result);
        verify(productRepository).findById(productId);
        verify(wareHouseRepository).findById(warehouseId);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void createInventory_ShouldFail_WhenProductNotFound() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.createInventory(inventoryRequestDto));

        verify(productRepository).findById(productId);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void createInventory_ShouldFail_WhenWarehouseNotFound() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wareHouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.createInventory(inventoryRequestDto));

        verify(wareHouseRepository).findById(warehouseId);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void createInventory_ShouldFail_WhenInventoryAlreadyExists() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wareHouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.existsByProduct_IdAndWarehouse_Id(productId, warehouseId)).thenReturn(true);

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class,
                () -> inventoryService.createInventory(inventoryRequestDto));

        assertTrue(exception.getMessage().contains("already exists"));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void getInventoryById_ShouldSucceed_WhenInventoryExists() {
        // Given
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));

        // When
        InventoryResponseDto result = inventoryService.getInventoryById(inventoryId);

        // Then
        assertNotNull(result);
        verify(inventoryRepository).findById(inventoryId);
    }

    @Test
    void getInventoryById_ShouldFail_WhenInventoryNotFound() {
        // Given
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.getInventoryById(inventoryId));

        verify(inventoryRepository).findById(inventoryId);
    }

    @Test
    void getAllInventories_ShouldReturnList() {
        // Given
        Inventory inventory2 = new Inventory();
        inventory2.setId(UUID.randomUUID());
        inventory2.setProduct(product);
        inventory2.setWarehouse(warehouse);
        inventory2.setQtyOnHand(50);
        inventory2.setQtyReserved(5);
        
        when(inventoryRepository.findAll()).thenReturn(Arrays.asList(inventory, inventory2));

        // When
        List<InventoryResponseDto> result = inventoryService.getAllInventories();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(inventoryRepository).findAll();
    }

    @Test
    void getByProductId_ShouldReturnList() {
        // Given
        when(inventoryRepository.findByProduct_Id(productId)).thenReturn(Arrays.asList(inventory));

        // When
        List<InventoryResponseDto> result = inventoryService.getByProductId(productId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inventoryRepository).findByProduct_Id(productId);
    }

    @Test
    void getByWarehouseId_ShouldReturnList() {
        // Given
        when(inventoryRepository.findByWarehouse_Id(warehouseId)).thenReturn(Arrays.asList(inventory));

        // When
        List<InventoryResponseDto> result = inventoryService.getByWarehouseId(warehouseId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inventoryRepository).findByWarehouse_Id(warehouseId);
    }

    @Test
    void createMovement_ShouldIncreaseStock_WhenInbound() {
        // Given
        InventoryMovementRequestDto movementDto = new InventoryMovementRequestDto();
        movementDto.setProductId(productId);
        movementDto.setWarehouseId(warehouseId);
        movementDto.setType(MovementType.INBOUND);
        movementDto.setQuantity(50L);
        movementDto.setOccurredAt(Instant.now());

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wareHouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(Arrays.asList(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMovmentRepository.save(any(InventoryMovement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // When
        InventoryMovementResponseDto result = inventoryService.createMovement(movementDto);

        // Then
        assertNotNull(result);
        verify(inventoryRepository).save(argThat(inv -> inv.getQtyOnHand() == 150)); // 100 + 50
    }

    @Test
    void createMovement_ShouldDecreaseStock_WhenOutbound() {
        // Given
        InventoryMovementRequestDto movementDto = new InventoryMovementRequestDto();
        movementDto.setProductId(productId);
        movementDto.setWarehouseId(warehouseId);
        movementDto.setType(MovementType.OUTBOUND);
        movementDto.setQuantity(30L);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wareHouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(Arrays.asList(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMovmentRepository.save(any(InventoryMovement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // When
        InventoryMovementResponseDto result = inventoryService.createMovement(movementDto);

        // Then
        assertNotNull(result);
        verify(inventoryRepository).save(argThat(inv -> inv.getQtyOnHand() == 70)); // 100 - 30
    }

    @Test
    void createMovement_ShouldFail_WhenOutboundExceedsStock() {
        // Given
        InventoryMovementRequestDto movementDto = new InventoryMovementRequestDto();
        movementDto.setProductId(productId);
        movementDto.setWarehouseId(warehouseId);
        movementDto.setType(MovementType.OUTBOUND);
        movementDto.setQuantity(150L); // More than available

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wareHouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(Arrays.asList(inventory));

        // When & Then
        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> inventoryService.createMovement(movementDto));

        assertTrue(exception.getMessage().contains("Stock insuffisant"));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void createMovement_ShouldAdjustStock_WhenAdjustment() {
        // Given
        InventoryMovementRequestDto movementDto = new InventoryMovementRequestDto();
        movementDto.setProductId(productId);
        movementDto.setWarehouseId(warehouseId);
        movementDto.setType(MovementType.ADJUSTMENT);
        movementDto.setQuantity(20L); // Positive adjustment

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wareHouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(Arrays.asList(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMovmentRepository.save(any(InventoryMovement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // When
        InventoryMovementResponseDto result = inventoryService.createMovement(movementDto);

        // Then
        assertNotNull(result);
        verify(inventoryRepository).save(argThat(inv -> inv.getQtyOnHand() == 120)); // 100 + 20
    }

    @Test
    void createMovement_ShouldFail_WhenAdjustmentLeadsToNegative() {
        // Given
        InventoryMovementRequestDto movementDto = new InventoryMovementRequestDto();
        movementDto.setProductId(productId);
        movementDto.setWarehouseId(warehouseId);
        movementDto.setType(MovementType.ADJUSTMENT);
        movementDto.setQuantity(-150L); // Negative adjustment exceeding stock

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wareHouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(Arrays.asList(inventory));

        // When & Then
        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> inventoryService.createMovement(movementDto));

        assertTrue(exception.getMessage().contains("stock négatif"));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void createMovement_ShouldCreateInventory_WhenNotExists() {
        // Given
        InventoryMovementRequestDto movementDto = new InventoryMovementRequestDto();
        movementDto.setProductId(productId);
        movementDto.setWarehouseId(warehouseId);
        movementDto.setType(MovementType.INBOUND);
        movementDto.setQuantity(50L);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wareHouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(Arrays.asList()); // No existing inventory
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMovmentRepository.save(any(InventoryMovement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // When
        InventoryMovementResponseDto result = inventoryService.createMovement(movementDto);

        // Then
        assertNotNull(result);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void updateInventory_ShouldSucceed_WhenValidData() {
        // Given
        inventoryRequestDto.setQtyOnHand(200L);
        inventoryRequestDto.setQtyReserved(20L);
        inventoryRequestDto.setProductId(productId); // Same product
        inventoryRequestDto.setWarehouseId(warehouseId); // Same warehouse

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // When
        InventoryResponseDto result = inventoryService.updateInventory(inventoryId, inventoryRequestDto);

        // Then
        assertNotNull(result);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void updateInventory_ShouldFail_WhenInventoryNotFound() {
        // Given
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.updateInventory(inventoryId, inventoryRequestDto));

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void updateInventory_ShouldUpdateProduct_WhenProductChanged() {
        // Given
        UUID newProductId = UUID.randomUUID();
        inventoryRequestDto.setProductId(newProductId);
        
        Product newProduct = new Product();
        newProduct.setId(newProductId);

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(productRepository.findById(newProductId)).thenReturn(Optional.of(newProduct));
        when(inventoryRepository.existsByProduct_IdAndWarehouse_Id(newProductId, warehouseId)).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // When
        InventoryResponseDto result = inventoryService.updateInventory(inventoryId, inventoryRequestDto);

        // Then
        assertNotNull(result);
        verify(productRepository).findById(newProductId);
    }

    @Test
    void updateInventory_ShouldFail_WhenNewProductNotFound() {
        // Given
        UUID newProductId = UUID.randomUUID();
        inventoryRequestDto.setProductId(newProductId);

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(productRepository.findById(newProductId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.updateInventory(inventoryId, inventoryRequestDto));
    }

    @Test
    void updateInventory_ShouldFail_WhenNewProductAlreadyExists() {
        // Given
        UUID newProductId = UUID.randomUUID();
        inventoryRequestDto.setProductId(newProductId);
        
        Product newProduct = new Product();
        newProduct.setId(newProductId);

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(productRepository.findById(newProductId)).thenReturn(Optional.of(newProduct));
        when(inventoryRepository.existsByProduct_IdAndWarehouse_Id(newProductId, warehouseId)).thenReturn(true);

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class,
                () -> inventoryService.updateInventory(inventoryId, inventoryRequestDto));

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void updateInventory_ShouldUpdateWarehouse_WhenWarehouseChanged() {
        // Given
        UUID newWarehouseId = UUID.randomUUID();
        inventoryRequestDto.setWarehouseId(newWarehouseId);
        
        WareHouse newWarehouse = new WareHouse();
        newWarehouse.setId(newWarehouseId);

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(wareHouseRepository.findById(newWarehouseId)).thenReturn(Optional.of(newWarehouse));
        when(inventoryRepository.existsByProduct_IdAndWarehouse_Id(productId, newWarehouseId)).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // When
        InventoryResponseDto result = inventoryService.updateInventory(inventoryId, inventoryRequestDto);

        // Then
        assertNotNull(result);
        verify(wareHouseRepository).findById(newWarehouseId);
    }

    @Test
    void updateInventory_ShouldFail_WhenNewWarehouseAlreadyExists() {
        // Given
        UUID newWarehouseId = UUID.randomUUID();
        inventoryRequestDto.setWarehouseId(newWarehouseId);
        
        WareHouse newWarehouse = new WareHouse();
        newWarehouse.setId(newWarehouseId);

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(wareHouseRepository.findById(newWarehouseId)).thenReturn(Optional.of(newWarehouse));
        when(inventoryRepository.existsByProduct_IdAndWarehouse_Id(productId, newWarehouseId)).thenReturn(true);

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class,
                () -> inventoryService.updateInventory(inventoryId, inventoryRequestDto));

        assertTrue(exception.getMessage().contains("already exists"));
    }
}
