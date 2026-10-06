package com.retailpos.product.entity;

/** Product unit of measure. Stored as VARCHAR(10) via {@code @Enumerated(EnumType.STRING)}. */
public enum UnitOfMeasure {
    PCS,
    KG,
    GRAM,
    LITER,
    BOX,
    PACK
}
