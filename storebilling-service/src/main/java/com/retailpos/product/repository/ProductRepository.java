package com.retailpos.product.repository;

import com.retailpos.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsByOrganizationIdAndSku(Long organizationId, String sku);

    boolean existsByOrganizationIdAndBarcode(Long organizationId, String barcode);

    Page<Product> findAllByOrganizationId(Long organizationId, Pageable pageable);

    Page<Product> findAllByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findAllByOrganizationIdAndCategoryId(Long organizationId, Long categoryId, Pageable pageable);

    /** Returns a List (not Optional) defensively - sku is unique per-org by DB constraint, but this stays safe even if that ever changes. */
    List<Product> findByOrganizationIdAndSku(Long organizationId, String sku);

    /** Returns a List (not Optional) defensively - barcode is unique per-org by DB constraint, but this stays safe even if that ever changes. */
    List<Product> findByOrganizationIdAndBarcode(Long organizationId, String barcode);

    /**
     * Barcode-scan/manual-code lookup: matches either sku or barcode for the organization.
     * Defensive {@code List} return - sku/barcode are each unique per-org, but not cross-checked
     * against each other, so in theory both a sku and a different product's barcode could match
     * the same code.
     */
    @Query("SELECT p FROM Product p WHERE p.organization.id = :organizationId AND (p.sku = :code OR p.barcode = :code)")
    List<Product> findByOrganizationIdAndSkuOrBarcode(@Param("organizationId") Long organizationId,
                                                        @Param("code") String code);
}
