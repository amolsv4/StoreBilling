package com.retailpos.stocktake.service;

import com.retailpos.common.exception.BusinessException;
import com.retailpos.inventory.service.InventoryService;
import com.retailpos.inventory.service.InventoryTransactionType;
import com.retailpos.inventory.service.StockChangeResult;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.product.entity.Product;
import com.retailpos.product.repository.ProductRepository;
import com.retailpos.stocktake.dto.StockTakeLineResponse;
import com.retailpos.stocktake.entity.StockTake;
import com.retailpos.stocktake.entity.StockTakeLine;
import com.retailpos.stocktake.entity.StockTakeStatus;
import com.retailpos.stocktake.mapper.StockTakeLineMapper;
import com.retailpos.stocktake.mapper.StockTakeMapper;
import com.retailpos.stocktake.repository.StockTakeLineRepository;
import com.retailpos.stocktake.repository.StockTakeRepository;
import com.retailpos.store.entity.Store;
import com.retailpos.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockTakeServiceImplTest {

    @Mock
    private StockTakeRepository stockTakeRepository;
    @Mock
    private StockTakeLineRepository stockTakeLineRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockTakeMapper stockTakeMapper;
    @Mock
    private StockTakeLineMapper stockTakeLineMapper;
    @Mock
    private InventoryService inventoryService;

    private StockTakeServiceImpl stockTakeService;

    @BeforeEach
    void setUp() {
        stockTakeService = new StockTakeServiceImpl(
                stockTakeRepository, stockTakeLineRepository, organizationRepository, storeRepository,
                productRepository, stockTakeMapper, stockTakeLineMapper, inventoryService);
        // lenient: not every test's code path reaches a line-mapping call (e.g. the "no lines"
        // rejection tests short-circuit before mapping anything).
        lenient().when(stockTakeLineMapper.toResponse(any())).thenReturn(
                new StockTakeLineResponse(null, null, null, null, null, null, null));
    }

    private StockTake draftStockTake() {
        Organization organization = new Organization();
        organization.setId(1L);
        Store store = new Store();
        store.setId(30L);

        StockTake stockTake = new StockTake();
        stockTake.setId(60L);
        stockTake.setOrganization(organization);
        stockTake.setStore(store);
        stockTake.setStatus(StockTakeStatus.DRAFT);
        stockTake.setStockTakeNumber("ST-0001");
        return stockTake;
    }

    private StockTakeLine line(Long productId, BigDecimal systemQuantitySnapshottedAtAddTime, BigDecimal countedQuantity) {
        Product product = new Product();
        product.setId(productId);
        StockTakeLine line = new StockTakeLine();
        line.setId(300L);
        line.setProduct(product);
        line.setSystemQuantity(systemQuantitySnapshottedAtAddTime);
        line.setCountedQuantity(countedQuantity);
        // Stale draft-time variance, computed when the line was added - complete() must ignore this
        // and recompute against the live balance instead.
        line.setVariance(countedQuantity.subtract(systemQuantitySnapshottedAtAddTime));
        return line;
    }

    @Test
    void completeRecomputesVarianceAgainstTheLiveBalanceNotTheStaleDraftTimeSnapshot() {
        StockTake stockTake = draftStockTake();
        // Counted 100 when the draft-time system quantity was 90 (draft-time variance = +10), but
        // by completion time sales have dropped the live balance to 95 - the true variance is +5,
        // not +10.
        StockTakeLine countLine = line(1L, new BigDecimal("90.000"), new BigDecimal("100.000"));

        when(stockTakeRepository.findById(60L)).thenReturn(Optional.of(stockTake));
        when(stockTakeLineRepository.findAllByStockTakeId(60L)).thenReturn(List.of(countLine));
        when(inventoryService.getCurrentStock(1L, 30L)).thenReturn(new BigDecimal("95.000"));
        when(inventoryService.applyStockChange(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new StockChangeResult(1L, new BigDecimal("100.000")));
        when(stockTakeRepository.save(any(StockTake.class))).thenAnswer(invocation -> invocation.getArgument(0));

        stockTakeService.complete(60L);

        assertThat(countLine.getVariance()).isEqualByComparingTo("5.000");
        verify(inventoryService).applyStockChange(1L, 30L, new BigDecimal("5.000"),
                InventoryTransactionType.STOCK_TAKE, "STOCK_TAKE", 60L, null);

        ArgumentCaptor<StockTakeLine> savedLine = ArgumentCaptor.forClass(StockTakeLine.class);
        verify(stockTakeLineRepository).save(savedLine.capture());
        assertThat(savedLine.getValue().getVariance()).isEqualByComparingTo("5.000");
    }

    @Test
    void completeSkipsPostingALedgerEntryWhenTheRecomputedVarianceIsZero() {
        StockTake stockTake = draftStockTake();
        // Draft-time variance looked non-zero (100 counted vs 90 system), but the live balance has
        // since caught up to exactly the counted quantity - no drift remains, so nothing should post.
        StockTakeLine countLine = line(1L, new BigDecimal("90.000"), new BigDecimal("100.000"));

        when(stockTakeRepository.findById(60L)).thenReturn(Optional.of(stockTake));
        when(stockTakeLineRepository.findAllByStockTakeId(60L)).thenReturn(List.of(countLine));
        when(inventoryService.getCurrentStock(1L, 30L)).thenReturn(new BigDecimal("100.000"));
        when(stockTakeRepository.save(any(StockTake.class))).thenAnswer(invocation -> invocation.getArgument(0));

        stockTakeService.complete(60L);

        assertThat(countLine.getVariance()).isEqualByComparingTo("0.000");
        verify(inventoryService, never()).applyStockChange(any(), any(), any(), any(), any(), any(), any());
        verify(stockTakeLineRepository).save(countLine);
    }

    @Test
    void completePostsOnlyForLinesWithNonZeroRecomputedVariance() {
        StockTake stockTake = draftStockTake();
        StockTakeLine noVarianceLine = line(1L, new BigDecimal("10.000"), new BigDecimal("10.000"));
        StockTakeLine variantLine = line(2L, new BigDecimal("10.000"), new BigDecimal("8.000"));

        when(stockTakeRepository.findById(60L)).thenReturn(Optional.of(stockTake));
        when(stockTakeLineRepository.findAllByStockTakeId(60L)).thenReturn(List.of(noVarianceLine, variantLine));
        when(inventoryService.getCurrentStock(1L, 30L)).thenReturn(new BigDecimal("10.000"));
        when(inventoryService.getCurrentStock(2L, 30L)).thenReturn(new BigDecimal("10.000"));
        when(inventoryService.applyStockChange(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new StockChangeResult(1L, BigDecimal.ZERO));
        when(stockTakeRepository.save(any(StockTake.class))).thenAnswer(invocation -> invocation.getArgument(0));

        stockTakeService.complete(60L);

        verify(inventoryService, times(1)).applyStockChange(any(), any(), any(), any(), any(), any(), any());
        verify(inventoryService).applyStockChange(2L, 30L, new BigDecimal("-2.000"),
                InventoryTransactionType.STOCK_TAKE, "STOCK_TAKE", 60L, null);
    }

    @Test
    void completeRejectsAStockTakeWithNoLines() {
        StockTake stockTake = draftStockTake();
        when(stockTakeRepository.findById(60L)).thenReturn(Optional.of(stockTake));
        when(stockTakeLineRepository.findAllByStockTakeId(60L)).thenReturn(List.of());

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> stockTakeService.complete(60L))
                .withMessageContaining("no lines");
    }

    @Test
    void addLineSnapshotsSystemQuantityFromTheLiveBalanceServerSide() {
        StockTake stockTake = draftStockTake();
        Product product = new Product();
        product.setId(1L);
        Organization organization = new Organization();
        organization.setId(1L);
        product.setOrganization(organization);

        when(stockTakeRepository.findById(60L)).thenReturn(Optional.of(stockTake));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryService.getCurrentStock(1L, 30L)).thenReturn(new BigDecimal("42.000"));
        when(stockTakeLineRepository.findAllByStockTakeId(60L)).thenReturn(List.of());

        stockTakeService.addLine(60L, new com.retailpos.stocktake.dto.CreateStockTakeLineRequest(1L, new BigDecimal("50.000")));

        ArgumentCaptor<StockTakeLine> savedLine = ArgumentCaptor.forClass(StockTakeLine.class);
        verify(stockTakeLineRepository).save(savedLine.capture());
        assertThat(savedLine.getValue().getSystemQuantity()).isEqualByComparingTo("42.000");
        assertThat(savedLine.getValue().getVariance()).isEqualByComparingTo("8.000");
    }
}
