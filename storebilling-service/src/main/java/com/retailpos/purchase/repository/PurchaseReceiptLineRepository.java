package com.retailpos.purchase.repository;

import com.retailpos.purchase.entity.PurchaseReceiptLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseReceiptLineRepository extends JpaRepository<PurchaseReceiptLine, Long> {

    List<PurchaseReceiptLine> findAllByPurchaseReceiptId(Long purchaseReceiptId);

    Optional<PurchaseReceiptLine> findByIdAndPurchaseReceiptId(Long id, Long purchaseReceiptId);

    void deleteAllByPurchaseReceiptId(Long purchaseReceiptId);
}
