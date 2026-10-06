package com.retailpos.sale.service;

import com.retailpos.common.exception.BusinessException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.configuration.service.ConfigContext;
import com.retailpos.configuration.service.ConfigurationService;
import com.retailpos.configuration.service.ResolvedConfig;
import com.retailpos.customer.entity.Customer;
import com.retailpos.customer.repository.CustomerRepository;
import com.retailpos.inventory.service.InventoryService;
import com.retailpos.inventory.service.InventoryTransactionType;
import com.retailpos.payment.service.PaymentService;
import com.retailpos.pricing.service.PricingResolutionService;
import com.retailpos.pricing.service.ResolvedPrice;
import com.retailpos.product.entity.Product;
import com.retailpos.product.repository.ProductRepository;
import com.retailpos.sale.dto.AddSaleLineRequest;
import com.retailpos.sale.dto.CompleteSaleResponse;
import com.retailpos.sale.dto.CreateSaleRequest;
import com.retailpos.sale.dto.SaleLineResponse;
import com.retailpos.sale.dto.SaleResponse;
import com.retailpos.sale.dto.UpdateSaleDiscountRequest;
import com.retailpos.sale.dto.UpdateSaleLineRequest;
import com.retailpos.sale.entity.Sale;
import com.retailpos.sale.entity.SaleLine;
import com.retailpos.sale.entity.SaleStatus;
import com.retailpos.sale.mapper.SaleMapper;
import com.retailpos.sale.repository.SaleLineRepository;
import com.retailpos.sale.repository.SaleRepository;
import com.retailpos.sale.repository.SaleSpecifications;
import com.retailpos.store.entity.Store;
import com.retailpos.store.repository.StoreRepository;
import com.retailpos.tax.entity.TaxGroup;
import com.retailpos.tax.repository.TaxGroupRepository;
import com.retailpos.tax.service.TaxGroupService;
import com.retailpos.terminal.entity.Terminal;
import com.retailpos.terminal.repository.TerminalRepository;
import com.retailpos.user.entity.User;
import com.retailpos.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Sale/SaleLine business logic. See {@link SaleService} for the lifecycle contract.
 *
 * <p>Totals derivation (documented since the plan does not spell out the exact header-total
 * formula): each line's {@code lineTotal} already correctly reflects its own tax treatment
 * (exclusive adds tax on top; inclusive leaves it unchanged, tax merely broken out for display).
 * The sale header's {@code totalAmount} is therefore {@code sum(line.lineTotal) - discountAmount}
 * (the bill-level discount, separate from any already-applied line-level discounts).
 * {@code subtotal}/{@code taxAmount} are informational breakdowns (sum of each line's pre-tax
 * subtotal / tax portion) for receipt display - for a sale mixing inclusive and exclusive lines,
 * {@code subtotal + taxAmount} is not required to equal the line-total sum, exactly like a
 * receipt shows "tax already included above" as an informational callout that doesn't change the
 * payable total.
 */
@Service
public class SaleServiceImpl implements SaleService {

    private static final BigDecimal PRICE_OVERRIDE_TOLERANCE = new BigDecimal("0.01");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final SaleRepository saleRepository;
    private final SaleLineRepository saleLineRepository;
    private final StoreRepository storeRepository;
    private final TerminalRepository terminalRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final TaxGroupRepository taxGroupRepository;
    private final ConfigurationService configurationService;
    private final PricingResolutionService pricingResolutionService;
    private final TaxGroupService taxGroupService;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final InvoiceNumberService invoiceNumberService;
    private final SaleMapper saleMapper;

    public SaleServiceImpl(SaleRepository saleRepository,
                            SaleLineRepository saleLineRepository,
                            StoreRepository storeRepository,
                            TerminalRepository terminalRepository,
                            CustomerRepository customerRepository,
                            UserRepository userRepository,
                            ProductRepository productRepository,
                            TaxGroupRepository taxGroupRepository,
                            ConfigurationService configurationService,
                            PricingResolutionService pricingResolutionService,
                            TaxGroupService taxGroupService,
                            PaymentService paymentService,
                            InventoryService inventoryService,
                            InvoiceNumberService invoiceNumberService,
                            SaleMapper saleMapper) {
        this.saleRepository = saleRepository;
        this.saleLineRepository = saleLineRepository;
        this.storeRepository = storeRepository;
        this.terminalRepository = terminalRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.taxGroupRepository = taxGroupRepository;
        this.configurationService = configurationService;
        this.pricingResolutionService = pricingResolutionService;
        this.taxGroupService = taxGroupService;
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
        this.invoiceNumberService = invoiceNumberService;
        this.saleMapper = saleMapper;
    }

    @Override
    @Transactional
    public SaleResponse create(CreateSaleRequest request, Long soldByUserId) {
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Store", request.storeId()));
        Terminal terminal = terminalRepository.findById(request.terminalId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Terminal", request.terminalId()));
        if (!terminal.getStore().getId().equals(store.getId())) {
            throw new BusinessException(
                    "Terminal '" + terminal.getCode() + "' does not belong to store '" + store.getCode() + "'.");
        }

        Customer customer = null;
        if (request.customerId() != null) {
            customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Customer", request.customerId()));
            if (!customer.getOrganization().getId().equals(store.getOrganization().getId())) {
                throw new BusinessException(
                        "Customer '" + customer.getCode() + "' belongs to a different organization.");
            }
        }

        User soldByUser = userRepository.findById(soldByUserId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("User", soldByUserId));

        ConfigContext currencyContext = new ConfigContext(terminal.getId(), store.getId(),
                store.getOrganization().getId(), store.getBusinessType().getId());
        ResolvedConfig currencyConfig = configurationService.resolve("DEFAULT_CURRENCY", currencyContext);
        String currency = currencyConfig.found() ? currencyConfig.value() : "USD";

        Sale sale = new Sale();
        sale.setOrganization(store.getOrganization());
        sale.setStore(store);
        sale.setTerminal(terminal);
        sale.setCustomer(customer);
        sale.setSoldByUser(soldByUser);
        sale.setSaleDate(LocalDateTime.now());
        sale.setStatus(SaleStatus.DRAFT);
        sale.setCurrency(currency);
        sale.setSubtotal(BigDecimal.ZERO);
        sale.setDiscountAmount(BigDecimal.ZERO);
        sale.setTaxAmount(BigDecimal.ZERO);
        sale.setTotalAmount(BigDecimal.ZERO);
        sale.setNotes(request.notes());

        Sale saved = saleRepository.save(sale);
        return toResponseWithLines(saved, List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SaleResponse> list(SaleStatus status, Long storeId, Long terminalId, Pageable pageable) {
        Page<Sale> page = saleRepository.findAll(SaleSpecifications.filter(status, storeId, terminalId), pageable);
        return page.map(sale -> toResponseWithLines(sale, saleLineRepository.findAllBySaleId(sale.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getById(Long id) {
        Sale sale = getEntity(id);
        return toResponseWithLines(sale, saleLineRepository.findAllBySaleId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public SaleAccessContext getAccessContext(Long saleId) {
        Sale sale = getEntity(saleId);
        return new SaleAccessContext(sale.getStore().getId(), sale.getTerminal().getId());
    }

    @Override
    @Transactional
    public SaleResponse addLine(Long saleId, AddSaleLineRequest request, boolean callerHasPriceOverrideAuthority) {
        Sale sale = getEditableSale(saleId);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Product", request.productId()));

        Optional<ResolvedPrice> resolved = pricingResolutionService.resolveEffectivePrice(
                product.getId(), sale.getStore().getId(), LocalDate.now());

        BigDecimal unitPrice;
        boolean overridden;
        if (request.unitPrice() == null) {
            if (resolved.isEmpty()) {
                throw new BusinessException("No price could be resolved for product '" + product.getSku()
                        + "'; a unit price must be supplied explicitly.");
            }
            unitPrice = resolved.get().sellingPrice();
            overridden = false;
        } else {
            unitPrice = request.unitPrice();
            overridden = isOverride(resolved, unitPrice);
            if (overridden && !callerHasPriceOverrideAuthority) {
                throw new AccessDeniedException(
                        "SALES_PRICE_OVERRIDE authority is required to set a unit price that differs "
                                + "from the resolved price (or when no price could be resolved at all).");
            }
        }

        SaleLine line = new SaleLine();
        line.setSale(sale);
        line.setProduct(product);
        line.setProductName(product.getName());
        line.setSku(product.getSku());
        line.setUom(product.getUom());
        line.setQuantity(request.quantity());
        line.setUnitPrice(unitPrice);
        line.setPriceOverridden(overridden);
        line.setDiscountAmount(request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO);

        applyTaxSnapshot(line, product);
        recomputeLineAmounts(line, isInclusive(line.getTaxGroupId()));

        saleLineRepository.save(line);
        return recalculateAndSave(sale);
    }

    @Override
    @Transactional
    public SaleResponse updateLine(Long saleId, Long lineId, UpdateSaleLineRequest request,
                                    boolean callerHasPriceOverrideAuthority) {
        Sale sale = getEditableSale(saleId);
        SaleLine line = getLineOfSale(sale, lineId);

        if (request.quantity() != null) {
            line.setQuantity(request.quantity());
        }
        if (request.discountAmount() != null) {
            line.setDiscountAmount(request.discountAmount());
        }
        if (request.unitPrice() != null) {
            Optional<ResolvedPrice> resolved = pricingResolutionService.resolveEffectivePrice(
                    line.getProduct().getId(), sale.getStore().getId(), LocalDate.now());
            boolean overridden = isOverride(resolved, request.unitPrice());
            if (overridden && !callerHasPriceOverrideAuthority) {
                throw new AccessDeniedException(
                        "SALES_PRICE_OVERRIDE authority is required to set a unit price that differs "
                                + "from the resolved price (or when no price could be resolved at all).");
            }
            line.setUnitPrice(request.unitPrice());
            line.setPriceOverridden(overridden);
        }

        recomputeLineAmounts(line, isInclusive(line.getTaxGroupId()));
        saleLineRepository.save(line);
        return recalculateAndSave(sale);
    }

    @Override
    @Transactional
    public SaleResponse deleteLine(Long saleId, Long lineId) {
        Sale sale = getEditableSale(saleId);
        SaleLine line = getLineOfSale(sale, lineId);
        saleLineRepository.delete(line);
        return recalculateAndSave(sale);
    }

    @Override
    @Transactional
    public SaleResponse updateDiscount(Long saleId, UpdateSaleDiscountRequest request) {
        Sale sale = getEditableSale(saleId);
        sale.setDiscountAmount(request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO);
        return recalculateAndSave(sale);
    }

    @Override
    @Transactional
    public SaleResponse hold(Long saleId) {
        Sale sale = getEditableSale(saleId);
        if (sale.getStatus() != SaleStatus.DRAFT) {
            throw new BusinessException("Only a DRAFT sale can be held.");
        }
        sale.setStatus(SaleStatus.HELD);
        Sale saved = saleRepository.save(sale);
        return toResponseWithLines(saved, saleLineRepository.findAllBySaleId(saleId));
    }

    @Override
    @Transactional
    public SaleResponse resume(Long saleId) {
        Sale sale = getEditableSale(saleId);
        if (sale.getStatus() != SaleStatus.HELD) {
            throw new BusinessException("Only a HELD sale can be resumed.");
        }
        sale.setStatus(SaleStatus.DRAFT);
        Sale saved = saleRepository.save(sale);
        return toResponseWithLines(saved, saleLineRepository.findAllBySaleId(saleId));
    }

    @Override
    @Transactional
    public CompleteSaleResponse complete(Long saleId) {
        Sale sale = getEntity(saleId);
        List<SaleLine> lines = saleLineRepository.findAllBySaleId(saleId);
        BigDecimal amountPaid = paymentService.sumCompletedAmount(saleId);

        if (sale.getStatus() == SaleStatus.COMPLETED) {
            // Idempotent re-call (reconciliation note 5): return the existing state unchanged -
            // recompute nothing, reassign no invoice number, re-post no inventory.
            SaleResponse existing = toResponseWithLines(sale, lines);
            BigDecimal changeDue = amountPaid.subtract(sale.getTotalAmount());
            return new CompleteSaleResponse(existing, amountPaid, changeDue);
        }

        if (lines.isEmpty()) {
            throw new BusinessException("Cannot complete a sale with no line items.");
        }
        if (amountPaid.compareTo(sale.getTotalAmount()) < 0) {
            throw new BusinessException("Payments (" + amountPaid + ") do not cover the sale total ("
                    + sale.getTotalAmount() + ").");
        }

        String invoiceNumber = invoiceNumberService.generateNextInvoiceNumber(sale.getStore());
        sale.setInvoiceNumber(invoiceNumber);
        sale.setStatus(SaleStatus.COMPLETED);
        sale.setCompletedDate(LocalDateTime.now());
        Sale saved = saleRepository.save(sale);

        for (SaleLine line : lines) {
            inventoryService.applyStockChange(line.getProduct().getId(), saved.getStore().getId(),
                    line.getQuantity().negate(), InventoryTransactionType.SALE, "SALE", saved.getId(), null);
        }

        BigDecimal changeDue = amountPaid.subtract(saved.getTotalAmount());
        SaleResponse response = toResponseWithLines(saved, lines);
        return new CompleteSaleResponse(response, amountPaid, changeDue);
    }

    // ---- helpers -------------------------------------------------------------------------

    private boolean isOverride(Optional<ResolvedPrice> resolved, BigDecimal requestedPrice) {
        if (resolved.isEmpty()) {
            return true;
        }
        return resolved.get().sellingPrice().subtract(requestedPrice).abs().compareTo(PRICE_OVERRIDE_TOLERANCE) > 0;
    }

    private void applyTaxSnapshot(SaleLine line, Product product) {
        TaxGroup taxGroup = product.getTaxGroup();
        if (taxGroup == null) {
            line.setTaxGroupId(null);
            line.setTaxPercentage(BigDecimal.ZERO);
            return;
        }
        line.setTaxGroupId(taxGroup.getId());
        line.setTaxPercentage(taxGroupService.getEffectiveTotalPercentage(taxGroup.getId()));
    }

    /** Reads the *current* inclusive flag only to decide the calculation branch - the outcome (taxAmount/lineTotal) is what gets snapshotted, not this flag itself. */
    private boolean isInclusive(Long taxGroupId) {
        if (taxGroupId == null) {
            return false;
        }
        return taxGroupRepository.findById(taxGroupId).map(TaxGroup::isInclusive).orElse(false);
    }

    /**
     * Section 0 item 3: exclusive (default) adds tax on top of the post-discount subtotal;
     * inclusive treats that subtotal as tax-already-included and back-calculates the tax portion
     * for display only - the line total is unchanged.
     */
    private void recomputeLineAmounts(SaleLine line, boolean inclusive) {
        BigDecimal lineSubtotal = line.getQuantity().multiply(line.getUnitPrice())
                .subtract(line.getDiscountAmount())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal pct = line.getTaxPercentage();
        if (pct == null || pct.compareTo(BigDecimal.ZERO) == 0) {
            line.setTaxAmount(BigDecimal.ZERO);
            line.setLineTotal(lineSubtotal);
            return;
        }

        BigDecimal pctFraction = pct.divide(HUNDRED, 10, RoundingMode.HALF_UP);
        if (inclusive) {
            BigDecimal divisor = BigDecimal.ONE.add(pctFraction);
            BigDecimal preTaxAmount = lineSubtotal.divide(divisor, 10, RoundingMode.HALF_UP);
            BigDecimal taxAmount = lineSubtotal.subtract(preTaxAmount).setScale(2, RoundingMode.HALF_UP);
            line.setTaxAmount(taxAmount);
            line.setLineTotal(lineSubtotal);
        } else {
            BigDecimal taxAmount = lineSubtotal.multiply(pctFraction).setScale(2, RoundingMode.HALF_UP);
            line.setTaxAmount(taxAmount);
            line.setLineTotal(lineSubtotal.add(taxAmount));
        }
    }

    private BigDecimal lineSubtotalForHeader(SaleLine line) {
        return line.getQuantity().multiply(line.getUnitPrice())
                .subtract(line.getDiscountAmount())
                .setScale(2, RoundingMode.HALF_UP);
    }

    private SaleResponse recalculateAndSave(Sale sale) {
        List<SaleLine> lines = saleLineRepository.findAllBySaleId(sale.getId());

        BigDecimal subtotal = lines.stream().map(this::lineSubtotalForHeader).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxAmount = lines.stream().map(SaleLine::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal lineTotalSum = lines.stream().map(SaleLine::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        sale.setSubtotal(subtotal);
        sale.setTaxAmount(taxAmount);
        BigDecimal total = lineTotalSum.subtract(sale.getDiscountAmount());
        sale.setTotalAmount(total.max(BigDecimal.ZERO));

        Sale saved = saleRepository.save(sale);
        return toResponseWithLines(saved, lines);
    }

    private SaleLine getLineOfSale(Sale sale, Long lineId) {
        SaleLine line = saleLineRepository.findById(lineId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("SaleLine", lineId));
        if (!line.getSale().getId().equals(sale.getId())) {
            throw ResourceNotFoundException.forEntity("SaleLine", lineId);
        }
        return line;
    }

    private Sale getEditableSale(Long saleId) {
        Sale sale = getEntity(saleId);
        if (sale.getStatus() == SaleStatus.COMPLETED) {
            throw new BusinessException("Sale " + saleId + " is already completed and cannot be modified.");
        }
        return sale;
    }

    private Sale getEntity(Long saleId) {
        return saleRepository.findById(saleId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Sale", saleId));
    }

    private SaleResponse toResponseWithLines(Sale sale, List<SaleLine> lines) {
        SaleResponse base = saleMapper.toResponse(sale);
        List<SaleLineResponse> lineResponses = lines.stream().map(saleMapper::toLineResponse).toList();
        return new SaleResponse(base.id(), base.organizationId(), base.storeId(), base.storeCode(),
                base.terminalId(), base.terminalCode(), base.customerId(), base.customerName(),
                base.soldByUserId(), base.soldByUsername(), base.saleDate(), base.status(), base.invoiceNumber(),
                base.currency(), base.subtotal(), base.discountAmount(), base.taxAmount(), base.totalAmount(),
                base.notes(), base.completedDate(), lineResponses, base.createdDate(), base.updatedDate());
    }
}
