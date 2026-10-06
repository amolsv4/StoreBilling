package com.retailpos.stocktransfer.service;

import com.retailpos.common.exception.BusinessException;
import com.retailpos.inventory.service.InventoryService;
import com.retailpos.inventory.service.InventoryTransactionType;
import com.retailpos.inventory.service.StockChangeResult;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.product.entity.Product;
import com.retailpos.product.repository.ProductRepository;
import com.retailpos.stocktransfer.entity.StockTransfer;
import com.retailpos.stocktransfer.entity.StockTransferLine;
import com.retailpos.stocktransfer.entity.StockTransferStatus;
import com.retailpos.stocktransfer.mapper.StockTransferLineMapper;
import com.retailpos.stocktransfer.mapper.StockTransferMapper;
import com.retailpos.stocktransfer.repository.StockTransferLineRepository;
import com.retailpos.stocktransfer.repository.StockTransferRepository;
import com.retailpos.store.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockTransferServiceImplTest {

    @Mock
    private StockTransferRepository stockTransferRepository;
    @Mock
    private StockTransferLineRepository stockTransferLineRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private com.retailpos.store.repository.StoreRepository storeRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockTransferMapper stockTransferMapper;
    @Mock
    private StockTransferLineMapper stockTransferLineMapper;
    @Mock
    private InventoryService inventoryService;

    private StockTransferServiceImpl stockTransferService;

    @BeforeEach
    void setUp() {
        stockTransferService = new StockTransferServiceImpl(
                stockTransferRepository, stockTransferLineRepository, organizationRepository, storeRepository,
                productRepository, stockTransferMapper, stockTransferLineMapper, inventoryService);
    }

    private StockTransfer draftTransfer(Long fromStoreId, Long toStoreId) {
        Organization organization = new Organization();
        organization.setId(1L);
        Store fromStore = new Store();
        fromStore.setId(fromStoreId);
        fromStore.setOrganization(organization);
        Store toStore = new Store();
        toStore.setId(toStoreId);
        toStore.setOrganization(organization);

        StockTransfer transfer = new StockTransfer();
        transfer.setId(50L);
        transfer.setOrganization(organization);
        transfer.setFromStore(fromStore);
        transfer.setToStore(toStore);
        transfer.setStatus(StockTransferStatus.DRAFT);
        transfer.setTransferNumber("TR-0001");
        return transfer;
    }

    private StockTransferLine line(Long productId, BigDecimal quantity) {
        Product product = new Product();
        product.setId(productId);
        StockTransferLine line = new StockTransferLine();
        line.setId(200L);
        line.setProduct(product);
        line.setQuantity(quantity);
        return line;
    }

    @Test
    void completePostsTransferOutAtSourceAndTransferInAtDestinationForEveryLine() {
        StockTransfer transfer = draftTransfer(10L, 20L);
        StockTransferLine lineA = line(1L, new BigDecimal("5.000"));
        StockTransferLine lineB = line(2L, new BigDecimal("3.000"));

        when(stockTransferRepository.findById(50L)).thenReturn(Optional.of(transfer));
        when(stockTransferLineRepository.findAllByStockTransferId(50L)).thenReturn(List.of(lineA, lineB));
        when(stockTransferRepository.save(any(StockTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryService.applyStockChange(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new StockChangeResult(1L, BigDecimal.ZERO));
        when(stockTransferLineMapper.toResponse(any())).thenReturn(mock(com.retailpos.stocktransfer.dto.StockTransferLineResponse.class));

        stockTransferService.complete(50L);

        InOrder inOrder = inOrder(inventoryService);
        // Line A: TRANSFER_OUT at the source (negative quantity) then TRANSFER_IN at the destination.
        inOrder.verify(inventoryService).applyStockChange(1L, 10L, new BigDecimal("-5.000"),
                InventoryTransactionType.TRANSFER_OUT, "STOCK_TRANSFER", 50L, null);
        inOrder.verify(inventoryService).applyStockChange(1L, 20L, new BigDecimal("5.000"),
                InventoryTransactionType.TRANSFER_IN, "STOCK_TRANSFER", 50L, null);
        // Line B: same pattern.
        inOrder.verify(inventoryService).applyStockChange(2L, 10L, new BigDecimal("-3.000"),
                InventoryTransactionType.TRANSFER_OUT, "STOCK_TRANSFER", 50L, null);
        inOrder.verify(inventoryService).applyStockChange(2L, 20L, new BigDecimal("3.000"),
                InventoryTransactionType.TRANSFER_IN, "STOCK_TRANSFER", 50L, null);

        verify(inventoryService, times(4)).applyStockChange(any(), any(), any(), any(), any(), any(), any());
        assertThat(transfer.getStatus()).isEqualTo(StockTransferStatus.COMPLETED);
    }

    @Test
    void completeRejectsATransferWithNoLines() {
        StockTransfer transfer = draftTransfer(10L, 20L);
        when(stockTransferRepository.findById(50L)).thenReturn(Optional.of(transfer));
        when(stockTransferLineRepository.findAllByStockTransferId(50L)).thenReturn(List.of());

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> stockTransferService.complete(50L))
                .withMessageContaining("no lines");

        verify(inventoryService, never()).applyStockChange(any(), any(), any(), any(), any(), any(), any());
        verify(stockTransferRepository, never()).save(any());
    }

    @Test
    void completeRejectsAnAlreadyCompletedTransfer() {
        StockTransfer transfer = draftTransfer(10L, 20L);
        transfer.setStatus(StockTransferStatus.COMPLETED);
        when(stockTransferRepository.findById(50L)).thenReturn(Optional.of(transfer));

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> stockTransferService.complete(50L));

        verify(inventoryService, never()).applyStockChange(any(), any(), any(), any(), any(), any(), any());
    }
}
