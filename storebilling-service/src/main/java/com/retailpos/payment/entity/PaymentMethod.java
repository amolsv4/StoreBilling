package com.retailpos.payment.entity;

/** Tender type for a {@link PaymentTransaction}. Stored as VARCHAR(20) via {@code @Enumerated(EnumType.STRING)}. */
public enum PaymentMethod {
    CASH,
    CARD,
    UPI,
    WALLET,
    BANK_TRANSFER,
    CREDIT,
    GIFT_CARD,
    OTHER
}
