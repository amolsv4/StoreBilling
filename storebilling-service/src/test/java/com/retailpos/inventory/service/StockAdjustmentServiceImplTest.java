package com.retailpos.inventory.service;

import com.retailpos.common.exception.BusinessException;
import com.retailpos.inventory.dto.CreateStockAdjustmentRequest;
import com.retailpos.inventory.entity.StockAdjustment;
import com.retailpos.inventory.entity.StockAdjustmentType;
import com.retailpos.inventory.repository.InventoryTransactionRepository;
import com.retailpos.inventory.repository.StockAdjustmentRepository;
import com.retailpos.product.entity.Product;
import com.retailpos.product.repository.ProductRepository;
import com.retailpos.store.entity.Store;
import com.retailpos.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockAdjustmentServiceImplTest {

    @Mock
    private StockAdjustmentRepository stockAdjustmentRepository;
    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private InventoryService inventoryService;

    private StockAdjustmentServiceImpl stockAdjustmentService;

    @BeforeEach
    void setUp() {
        stockAdjustmentService = new StockAdjustmentServiceImpl(
                stockAdjustmentRepository, inventoryTransactionRepository, productRepository, storeRepository, inventoryService);
    }

    @ParameterizedTest
    @EnumSource(value = StockAdjustmentType.class, names = {"DAMAGE", "EXPIRY"})
    void createRejectsAPositiveQuantityChangeForDamageOrExpiry(StockAdjustmentType type) {
        CreateStockAdjustmentRequest request = new CreateStockAdjustmentRequest(
                1L, 2L, type, new BigDecimal("5.000"), "Found extra stock");

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> stockAdjustmentService.create(request))
                .withMessageContaining("negative damage");

        verify(stockAdjustmentRepository, never()).save(any());
        verify(inventoryService, never()).applyStockChange(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void createAllowsANegativeQuantityChangeForDamage() {
        CreateStockAdjustmentRequest request = new CreateStockAdjustmentRequest(
                1L, 2L, StockAdjustmentType.DAMAGE, new BigDecimal("-3.000"), "Dropped and broken");

        Product product = new Product();
        product.setId(2L);
        product.setSku("SKU-1");
        product.setName("Widget");
        Store store = new Store();
        store.setId(1L);
        store.setCode("ST-1");
        store.setName("Store One");

        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(stockAdjustmentRepository.save(any(StockAdjustment.class))).thenAnswer(invocation -> {
            StockAdjustment adjustment = invocation.getArgument(0);
            adjustment.setId(77L);
            return adjustment;
        });
        when(inventoryService.applyStockChange(eq(2L), eq(1L), eq(new BigDecimal("-3.000")),
                eq(InventoryTransactionType.DAMAGE), eq("STOCK_ADJUSTMENT"), eq(77L), eq("Dropped and broken")))
                .thenReturn(new StockChangeResult(500L, new BigDecimal("7.000")));

        var response = stockAdjustmentService.create(request);

        assertThat(response.quantityChange()).isEqualByComparingTo("-3.000");
        assertThat(response.balanceAfter()).isEqualByComparingTo("7.000");

        ArgumentCaptor<StockAdjustment> savedCaptor = ArgumentCaptor.forClass(StockAdjustment.class);
        verify(stockAdjustmentRepository).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().getAdjustmentType()).isEqualTo(StockAdjustmentType.DAMAGE);
    }

    @Test
    void createAllowsAPositiveQuantityChangeForPlainAdjustment() {
        CreateStockAdjustmentRequest request = new CreateStockAdjustmentRequest(
                1L, 2L, StockAdjustmentType.ADJUSTMENT, new BigDecimal("10.000"), "Recount correction");

        Product product = new Product();
        product.setId(2L);
        Store store = new Store();
        store.setId(1L);

        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(stockAdjustmentRepository.save(any(StockAdjustment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryService.applyStockChange(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new StockChangeResult(1L, new BigDecimal("10.000")));

        assertThat(stockAdjustmentService.create(request)).isNotNull();
        verify(inventoryService).applyStockChange(2L, 1L, new BigDecimal("10.000"),
                InventoryTransactionType.ADJUSTMENT, "STOCK_ADJUSTMENT", null, "Recount correction");
    }
}
