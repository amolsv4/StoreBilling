package com.retailpos.inventory.service;

import java.math.BigDecimal;

/** Result of a single {@link InventoryService#applyStockChange} call. */
public record StockChangeResult(Long transactionId, BigDecimal balanceAfter) {
}
