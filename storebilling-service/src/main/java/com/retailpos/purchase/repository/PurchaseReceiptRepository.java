package com.retailpos.purchase.repository;

import com.retailpos.purchase.entity.PurchaseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PurchaseReceiptRepository
        extends JpaRepository<PurchaseReceipt, Long>, JpaSpecificationExecutor<PurchaseReceipt> {

    boolean existsByOrganizationIdAndReceiptNumber(Long organizationId, String receiptNumber);
}
