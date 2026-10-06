package com.retailpos.tax.entity;

/** Tax component category. Stored as VARCHAR(20) via {@code @Enumerated(EnumType.STRING)}. */
public enum TaxType {
    CGST,
    SGST,
    IGST,
    CESS,
    GENERAL
}
