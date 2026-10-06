package com.retailpos.pricing.service;

import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.pricing.dto.CreateProductPriceRequest;
import com.retailpos.pricing.dto.ProductPriceResponse;
import com.retailpos.pricing.dto.UpdateProductPriceRequest;
import com.retailpos.pricing.entity.PriceList;
import com.retailpos.pricing.entity.ProductPrice;
import com.retailpos.pricing.mapper.ProductPriceMapper;
import com.retailpos.pricing.repository.PriceListRepository;
import com.retailpos.pricing.repository.ProductPriceRepository;
import com.retailpos.product.entity.Product;
import com.retailpos.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductPriceServiceImpl implements ProductPriceService {

    private final ProductPriceRepository productPriceRepository;
    private final PriceListRepository priceListRepository;
    private final ProductRepository productRepository;
    private final ProductPriceMapper productPriceMapper;

    public ProductPriceServiceImpl(ProductPriceRepository productPriceRepository,
                                    PriceListRepository priceListRepository,
                                    ProductRepository productRepository,
                                    ProductPriceMapper productPriceMapper) {
        this.productPriceRepository = productPriceRepository;
        this.priceListRepository = priceListRepository;
        this.productRepository = productRepository;
        this.productPriceMapper = productPriceMapper;
    }

    @Override
    @Transactional
    public ProductPriceResponse create(CreateProductPriceRequest request) {
        if (productPriceRepository.existsByPriceListIdAndProductId(request.priceListId(), request.productId())) {
            throw new DuplicateResourceException(
                    "A price already exists for product id " + request.productId()
                            + " in price list id " + request.priceListId() + ".");
        }
        PriceList priceList = priceListRepository.findById(request.priceListId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("PriceList", request.priceListId()));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Product", request.productId()));

        ProductPrice productPrice = productPriceMapper.toEntity(request);
        productPrice.setPriceList(priceList);
        productPrice.setProduct(product);
        ProductPrice saved = productPriceRepository.save(productPrice);
        return productPriceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductPriceResponse update(Long id, UpdateProductPriceRequest request) {
        ProductPrice productPrice = getEntity(id);
        productPriceMapper.updateEntityFromRequest(request, productPrice);
        if (request.active() != null) {
            productPrice.setActive(request.active());
        }
        ProductPrice saved = productPriceRepository.save(productPrice);
        return productPriceMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPriceResponse getById(Long id) {
        return productPriceMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductPriceResponse> list(Long priceListId, Long productId, Pageable pageable) {
        Page<ProductPrice> page;
        if (priceListId != null && productId != null) {
            page = productPriceRepository.findAllByPriceListIdAndProductId(priceListId, productId, pageable);
        } else if (priceListId != null) {
            page = productPriceRepository.findAllByPriceListId(priceListId, pageable);
        } else if (productId != null) {
            page = productPriceRepository.findAllByProductId(productId, pageable);
        } else {
            page = productPriceRepository.findAll(pageable);
        }
        return page.map(productPriceMapper::toResponse);
    }

    private ProductPrice getEntity(Long id) {
        return productPriceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("ProductPrice", id));
    }
}
