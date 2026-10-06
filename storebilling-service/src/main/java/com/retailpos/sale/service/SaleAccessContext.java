package com.retailpos.sale.service;

/**
 * Lightweight (storeId, terminalId) projection of an existing sale, used by controllers to run
 * {@code AccessValidator} checks keyed off the *loaded* sale's store/terminal rather than a URL
 * param, so revoked store access takes effect immediately even on sub-resource calls (lines,
 * payments, notifications, ...).
 */
public record SaleAccessContext(Long storeId, Long terminalId) {
}
