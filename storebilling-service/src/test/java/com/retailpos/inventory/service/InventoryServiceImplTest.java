package com.retailpos.inventory.service;

import com.retailpos.inventory.entity.Inventory;
import com.retailpos.inventory.entity.InventoryTransaction;
import com.retailpos.inventory.repository.InventoryRepository;
import com.retailpos.inventory.repository.InventoryTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(inventoryRepository, inventoryTransactionRepository);
    }

    private Inventory balance(BigDecimal quantityOnHand) {
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setProductId(10L);
        inventory.setStoreId(20L);
        inventory.setQuantityOnHand(quantityOnHand);
        return inventory;
    }

    @Test
    void applyStockChangeAddsToAnExistingBalanceAndPostsTheLedgerEntry() {
        when(inventoryRepository.findByProductIdAndStoreIdForUpdate(10L, 20L))
                .thenReturn(Optional.of(balance(new BigDecimal("50.000"))));
        when(inventoryTransactionRepository.save(any(InventoryTransaction.class)))
                .thenAnswer(invocation -> {
                    InventoryTransaction transaction = invocation.getArgument(0);
                    transaction.setId(999L);
                    return transaction;
                });

        StockChangeResult result = inventoryService.applyStockChange(10L, 20L, new BigDecimal("25.000"),
                InventoryTransactionType.PURCHASE, "PURCHASE_RECEIPT", 5L, null);

        assertThat(result.balanceAfter()).isEqualByComparingTo("75.000");
        assertThat(result.transactionId()).isEqualTo(999L);

        ArgumentCaptor<Inventory> savedBalance = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(savedBalance.capture());
        assertThat(savedBalance.getValue().getQuantityOnHand()).isEqualByComparingTo("75.000");

        ArgumentCaptor<InventoryTransaction> savedTransaction = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(inventoryTransactionRepository).save(savedTransaction.capture());
        InventoryTransaction transaction = savedTransaction.getValue();
        assertThat(transaction.getBalanceAfter()).isEqualByComparingTo("75.000");
        assertThat(transaction.getQuantityChange()).isEqualByComparingTo("25.000");
        assertThat(transaction.getTransactionType()).isEqualTo(InventoryTransactionType.PURCHASE);
        assertThat(transaction.getReferenceType()).isEqualTo("PURCHASE_RECEIPT");
        assertThat(transaction.getReferenceId()).isEqualTo(5L);
    }

    @Test
    void applyStockChangeAllowsTheBalanceToGoNegativeWithoutThrowing() {
        when(inventoryRepository.findByProductIdAndStoreIdForUpdate(10L, 20L))
                .thenReturn(Optional.of(balance(new BigDecimal("5.000"))));
        when(inventoryTransactionRepository.save(any(InventoryTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StockChangeResult result = inventoryService.applyStockChange(10L, 20L, new BigDecimal("-20.000"),
                InventoryTransactionType.SALE, "SALE", 1L, null);

        assertThat(result.balanceAfter()).isEqualByComparingTo("-15.000");
    }

    @Test
    void applyStockChangeCreatesAZeroBalanceRowWhenNoneExistsYet() {
        when(inventoryRepository.findByProductIdAndStoreIdForUpdate(10L, 20L)).thenReturn(Optional.empty());
        when(inventoryRepository.saveAndFlush(any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryTransactionRepository.save(any(InventoryTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StockChangeResult result = inventoryService.applyStockChange(10L, 20L, new BigDecimal("40.000"),
                InventoryTransactionType.PURCHASE, "PURCHASE_RECEIPT", 1L, null);

        assertThat(result.balanceAfter()).isEqualByComparingTo("40.000");
        verify(inventoryRepository).saveAndFlush(any(Inventory.class));
    }

    @Test
    void applyStockChangeRetriesOnceWhenTheFirstInsertRacesAnotherCaller() {
        when(inventoryRepository.findByProductIdAndStoreIdForUpdate(10L, 20L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(balance(new BigDecimal("10.000"))));
        when(inventoryRepository.saveAndFlush(any(Inventory.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));
        when(inventoryTransactionRepository.save(any(InventoryTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StockChangeResult result = inventoryService.applyStockChange(10L, 20L, new BigDecimal("5.000"),
                InventoryTransactionType.PURCHASE, "PURCHASE_RECEIPT", 1L, null);

        assertThat(result.balanceAfter()).isEqualByComparingTo("15.000");
        verify(inventoryRepository, times(2)).findByProductIdAndStoreIdForUpdate(10L, 20L);
    }

    @Test
    void getCurrentStockReturnsZeroWhenNoBalanceRowExists() {
        when(inventoryRepository.findByProductIdAndStoreId(10L, 20L)).thenReturn(Optional.empty());

        BigDecimal stock = inventoryService.getCurrentStock(10L, 20L);

        assertThat(stock).isEqualByComparingTo("0.000");
        verify(inventoryRepository, never()).findByProductIdAndStoreIdForUpdate(any(), any());
    }

    @Test
    void getCurrentStockReturnsTheStoredBalance() {
        when(inventoryRepository.findByProductIdAndStoreId(10L, 20L))
                .thenReturn(Optional.of(balance(new BigDecimal("33.500"))));

        BigDecimal stock = inventoryService.getCurrentStock(10L, 20L);

        assertThat(stock).isEqualByComparingTo("33.500");
    }
}
