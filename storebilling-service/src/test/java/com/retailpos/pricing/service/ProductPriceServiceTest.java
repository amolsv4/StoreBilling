package com.retailpos.pricing.service;

import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.pricing.dto.CreateProductPriceRequest;
import com.retailpos.pricing.mapper.ProductPriceMapper;
import com.retailpos.pricing.repository.PriceListRepository;
import com.retailpos.pricing.repository.ProductPriceRepository;
import com.retailpos.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductPriceServiceTest {

    @Mock
    private ProductPriceRepository productPriceRepository;
    @Mock
    private PriceListRepository priceListRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductPriceMapper productPriceMapper;

    private ProductPriceServiceImpl productPriceService;

    @BeforeEach
    void setUp() {
        productPriceService = new ProductPriceServiceImpl(
                productPriceRepository, priceListRepository, productRepository, productPriceMapper);
    }

    @Test
    void createRejectsDuplicatePriceListAndProductCombination() {
        when(productPriceRepository.existsByPriceListIdAndProductId(10L, 20L)).thenReturn(true);

        CreateProductPriceRequest request = new CreateProductPriceRequest(
                10L, 20L, new BigDecimal("100.00"), new BigDecimal("120.00"), null, null);

        assertThatExceptionOfType(DuplicateResourceException.class)
                .isThrownBy(() -> productPriceService.create(request));

        verify(priceListRepository, never()).findById(any());
        verify(productRepository, never()).findById(any());
        verify(productPriceRepository, never()).save(any());
    }
}
