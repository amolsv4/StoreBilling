package com.retailpos.sale.service;

import com.retailpos.common.exception.BusinessException;
import com.retailpos.configuration.service.ConfigurationService;
import com.retailpos.customer.repository.CustomerRepository;
import com.retailpos.inventory.service.InventoryService;
import com.retailpos.inventory.service.InventoryTransactionType;
import com.retailpos.organization.entity.BusinessType;
import com.retailpos.organization.entity.Organization;
import com.retailpos.payment.service.PaymentService;
import com.retailpos.pricing.service.PricingResolutionService;
import com.retailpos.pricing.service.ResolvedPrice;
import com.retailpos.product.entity.Product;
import com.retailpos.product.entity.UnitOfMeasure;
import com.retailpos.product.repository.ProductRepository;
import com.retailpos.sale.dto.AddSaleLineRequest;
import com.retailpos.sale.dto.CompleteSaleResponse;
import com.retailpos.sale.dto.SaleLineResponse;
import com.retailpos.sale.dto.SaleResponse;
import com.retailpos.sale.entity.Sale;
import com.retailpos.sale.entity.SaleLine;
import com.retailpos.sale.entity.SaleStatus;
import com.retailpos.sale.mapper.SaleMapper;
import com.retailpos.sale.repository.SaleLineRepository;
import com.retailpos.sale.repository.SaleRepository;
import com.retailpos.store.entity.Store;
import com.retailpos.store.repository.StoreRepository;
import com.retailpos.tax.entity.TaxGroup;
import com.retailpos.tax.repository.TaxGroupRepository;
import com.retailpos.tax.service.TaxGroupService;
import com.retailpos.terminal.entity.Terminal;
import com.retailpos.terminal.repository.TerminalRepository;
import com.retailpos.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock private SaleRepository saleRepository;
    @Mock private SaleLineRepository saleLineRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private TerminalRepository terminalRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private TaxGroupRepository taxGroupRepository;
    @Mock private ConfigurationService configurationService;
    @Mock private PricingResolutionService pricingResolutionService;
    @Mock private TaxGroupService taxGroupService;
    @Mock private PaymentService paymentService;
    @Mock private InventoryService inventoryService;
    @Mock private InvoiceNumberService invoiceNumberService;
    @Mock private SaleMapper saleMapper;

    private SaleServiceImpl saleService;

    private Sale sale;
    private Product product;

    @BeforeEach
    void setUp() {
        saleService = new SaleServiceImpl(saleRepository, saleLineRepository, storeRepository, terminalRepository,
                customerRepository, userRepository, productRepository, taxGroupRepository, configurationService,
                pricingResolutionService, taxGroupService, paymentService, inventoryService, invoiceNumberService,
                saleMapper);

        Organization organization = new Organization();
        organization.setId(1L);

        BusinessType businessType = new BusinessType();
        businessType.setId(1L);

        Store store = new Store();
        store.setId(100L);
        store.setCode("STORE1");
        store.setOrganization(organization);
        store.setBusinessType(businessType);

        Terminal terminal = new Terminal();
        terminal.setId(200L);
        terminal.setCode("T1");
        terminal.setStore(store);

        sale = new Sale();
        sale.setId(500L);
        sale.setOrganization(organization);
        sale.setStore(store);
        sale.setTerminal(terminal);
        sale.setStatus(SaleStatus.DRAFT);
        sale.setCurrency("USD");
        sale.setSubtotal(BigDecimal.ZERO);
        sale.setDiscountAmount(BigDecimal.ZERO);
        sale.setTaxAmount(BigDecimal.ZERO);
        sale.setTotalAmount(BigDecimal.ZERO);

        product = new Product();
        product.setId(10L);
        product.setSku("SKU1");
        product.setName("Widget");
        product.setUom(UnitOfMeasure.PCS);

        lenient().when(saleRepository.findById(500L)).thenReturn(Optional.of(sale));
        lenient().when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(saleLineRepository.save(any(SaleLine.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(saleLineRepository.findAllBySaleId(500L)).thenReturn(List.of());
        lenient().when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        // Reflect the real entity's state rather than a fixed dummy, so tests that assert on the
        // returned SaleResponse (e.g. invoiceNumber/status/totalAmount after complete()) see the
        // actual computed values instead of a stale placeholder.
        lenient().when(saleMapper.toResponse(any(Sale.class))).thenAnswer(inv -> {
            Sale s = inv.getArgument(0);
            Long terminalId = s.getTerminal() != null ? s.getTerminal().getId() : null;
            String terminalCode = s.getTerminal() != null ? s.getTerminal().getCode() : null;
            return new SaleResponse(s.getId(), s.getOrganization().getId(), s.getStore().getId(), s.getStore().getCode(),
                    terminalId, terminalCode, null, null, null, null, s.getSaleDate(), s.getStatus(),
                    s.getInvoiceNumber(), s.getCurrency(), s.getSubtotal(), s.getDiscountAmount(), s.getTaxAmount(),
                    s.getTotalAmount(), s.getNotes(), s.getCompletedDate(), List.of(), s.getCreatedDate(), s.getUpdatedDate());
        });
        lenient().when(saleMapper.toLineResponse(any(SaleLine.class))).thenAnswer(inv -> {
            SaleLine l = inv.getArgument(0);
            Long productId = l.getProduct() != null ? l.getProduct().getId() : null;
            return new SaleLineResponse(l.getId(), productId, l.getProductName(), l.getSku(), l.getUom(),
                    l.getQuantity(), l.getUnitPrice(), l.isPriceOverridden(), l.getDiscountAmount(),
                    l.getTaxGroupId(), l.getTaxPercentage(), l.getTaxAmount(), l.getLineTotal());
        });
    }

    // ---- price-override permission enforcement ----------------------------------------

    @Test
    void addLineRejectsManualPriceDifferingFromResolvedWithoutOverrideAuthority() {
        when(pricingResolutionService.resolveEffectivePrice(eq(10L), eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.of(new ResolvedPrice(1L, new BigDecimal("10.00"), new BigDecimal("10.00"))));

        AddSaleLineRequest request = new AddSaleLineRequest(10L, BigDecimal.ONE, new BigDecimal("25.00"), null);

        assertThatExceptionOfType(AccessDeniedException.class)
                .isThrownBy(() -> saleService.addLine(500L, request, false));

        verify(saleLineRepository, never()).save(any());
    }

    @Test
    void addLineAllowsManualPriceDifferingFromResolvedWithOverrideAuthority() {
        when(pricingResolutionService.resolveEffectivePrice(eq(10L), eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.of(new ResolvedPrice(1L, new BigDecimal("10.00"), new BigDecimal("10.00"))));

        AddSaleLineRequest request = new AddSaleLineRequest(10L, BigDecimal.ONE, new BigDecimal("25.00"), null);

        saleService.addLine(500L, request, true);

        ArgumentCaptor<SaleLine> captor = ArgumentCaptor.forClass(SaleLine.class);
        verify(saleLineRepository).save(captor.capture());
        assertThat(captor.getValue().isPriceOverridden()).isTrue();
        assertThat(captor.getValue().getUnitPrice()).isEqualByComparingTo("25.00");
    }

    @Test
    void addLineWithNoResolvedPriceAndNoManualPriceFails() {
        when(pricingResolutionService.resolveEffectivePrice(eq(10L), eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        AddSaleLineRequest request = new AddSaleLineRequest(10L, BigDecimal.ONE, null, null);

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> saleService.addLine(500L, request, true));
        verify(saleLineRepository, never()).save(any());
    }

    @Test
    void addLineWithinToleranceIsNotTreatedAsOverride() {
        when(pricingResolutionService.resolveEffectivePrice(eq(10L), eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.of(new ResolvedPrice(1L, new BigDecimal("10.00"), new BigDecimal("10.00"))));

        // 10.005 differs from 10.00 by less than the 0.01 tolerance.
        AddSaleLineRequest request = new AddSaleLineRequest(10L, BigDecimal.ONE, new BigDecimal("10.005"), null);

        saleService.addLine(500L, request, false);

        ArgumentCaptor<SaleLine> captor = ArgumentCaptor.forClass(SaleLine.class);
        verify(saleLineRepository).save(captor.capture());
        assertThat(captor.getValue().isPriceOverridden()).isFalse();
    }

    // ---- tax inclusive vs exclusive line calculation -----------------------------------

    @Test
    void exclusiveTaxGroupAddsTaxOnTopOfSubtotal() {
        TaxGroup taxGroup = new TaxGroup();
        taxGroup.setId(900L);
        taxGroup.setInclusive(false);
        product.setTaxGroup(taxGroup);

        when(pricingResolutionService.resolveEffectivePrice(eq(10L), eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.of(new ResolvedPrice(1L, new BigDecimal("100.00"), new BigDecimal("100.00"))));
        when(taxGroupService.getEffectiveTotalPercentage(900L)).thenReturn(new BigDecimal("18.00"));
        when(taxGroupRepository.findById(900L)).thenReturn(Optional.of(taxGroup));

        AddSaleLineRequest request = new AddSaleLineRequest(10L, new BigDecimal("2"), new BigDecimal("100.00"), null);
        saleService.addLine(500L, request, false);

        ArgumentCaptor<SaleLine> captor = ArgumentCaptor.forClass(SaleLine.class);
        verify(saleLineRepository).save(captor.capture());
        SaleLine line = captor.getValue();

        // subtotal = 2 * 100.00 = 200.00; exclusive tax = 200.00 * 0.18 = 36.00; total = 236.00
        assertThat(line.getTaxAmount()).isEqualByComparingTo("36.00");
        assertThat(line.getLineTotal()).isEqualByComparingTo("236.00");
    }

    @Test
    void inclusiveTaxGroupBackCalculatesTaxWithoutChangingLineTotal() {
        TaxGroup taxGroup = new TaxGroup();
        taxGroup.setId(901L);
        taxGroup.setInclusive(true);
        product.setTaxGroup(taxGroup);

        when(pricingResolutionService.resolveEffectivePrice(eq(10L), eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.of(new ResolvedPrice(1L, new BigDecimal("100.00"), new BigDecimal("100.00"))));
        when(taxGroupService.getEffectiveTotalPercentage(901L)).thenReturn(new BigDecimal("18.00"));
        when(taxGroupRepository.findById(901L)).thenReturn(Optional.of(taxGroup));

        AddSaleLineRequest request = new AddSaleLineRequest(10L, new BigDecimal("2"), new BigDecimal("100.00"), null);
        saleService.addLine(500L, request, false);

        ArgumentCaptor<SaleLine> captor = ArgumentCaptor.forClass(SaleLine.class);
        verify(saleLineRepository).save(captor.capture());
        SaleLine line = captor.getValue();

        // subtotal = 200.00 (tax-inclusive); pre-tax = 200 / 1.18 = 169.4915254237...
        // tax = 200.00 - 169.4915254237 = 30.5084745763 -> rounds to 30.51; lineTotal unchanged.
        assertThat(line.getTaxAmount()).isEqualByComparingTo("30.51");
        assertThat(line.getLineTotal()).isEqualByComparingTo("200.00");
    }

    // ---- complete() idempotency ---------------------------------------------------------

    @Test
    void completeIsIdempotentOnASecondCall() {
        sale.setStatus(SaleStatus.COMPLETED);
        sale.setInvoiceNumber("STORE1-20260101-000001");
        sale.setTotalAmount(new BigDecimal("100.00"));

        when(paymentService.sumCompletedAmount(500L)).thenReturn(new BigDecimal("120.00"));

        CompleteSaleResponse first = saleService.complete(500L);
        CompleteSaleResponse second = saleService.complete(500L);

        assertThat(first.sale().invoiceNumber()).isEqualTo("STORE1-20260101-000001");
        assertThat(second.sale().invoiceNumber()).isEqualTo(first.sale().invoiceNumber());
        assertThat(first.changeDue()).isEqualByComparingTo("20.00");
        assertThat(second.changeDue()).isEqualByComparingTo("20.00");

        verify(invoiceNumberService, never()).generateNextInvoiceNumber(any());
        verify(inventoryService, never()).applyStockChange(anyLong(), anyLong(), any(), any(), any(), anyLong(), any());
        verify(saleRepository, never()).save(any());
    }

    // ---- payment-sum-covers-total validation --------------------------------------------

    @Test
    void completeRejectsWhenPaymentsDoNotCoverTotal() {
        sale.setStatus(SaleStatus.DRAFT);
        sale.setTotalAmount(new BigDecimal("100.00"));

        SaleLine existingLine = new SaleLine();
        existingLine.setId(1L);
        when(saleLineRepository.findAllBySaleId(500L)).thenReturn(List.of(existingLine));
        when(paymentService.sumCompletedAmount(500L)).thenReturn(new BigDecimal("50.00"));

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> saleService.complete(500L))
                .withMessageContaining("do not cover");

        verify(invoiceNumberService, never()).generateNextInvoiceNumber(any());
        verify(inventoryService, never()).applyStockChange(anyLong(), anyLong(), any(), any(), any(), anyLong(), any());
    }

    @Test
    void completeSucceedsWhenPaymentsExactlyCoverTotalAndPostsInventoryOncePerLine() {
        sale.setStatus(SaleStatus.DRAFT);
        sale.setTotalAmount(new BigDecimal("100.00"));

        SaleLine line = new SaleLine();
        line.setId(1L);
        line.setProduct(product);
        line.setQuantity(new BigDecimal("2.000"));
        when(saleLineRepository.findAllBySaleId(500L)).thenReturn(List.of(line));
        when(paymentService.sumCompletedAmount(500L)).thenReturn(new BigDecimal("100.00"));
        when(invoiceNumberService.generateNextInvoiceNumber(sale.getStore())).thenReturn("STORE1-20260101-000001");

        CompleteSaleResponse response = saleService.complete(500L);

        assertThat(sale.getStatus()).isEqualTo(SaleStatus.COMPLETED);
        assertThat(response.changeDue()).isEqualByComparingTo("0.00");
        verify(inventoryService).applyStockChange(eq(10L), eq(100L), eq(new BigDecimal("-2.000")),
                eq(InventoryTransactionType.SALE), eq("SALE"), eq(500L), isNull());
    }
}
