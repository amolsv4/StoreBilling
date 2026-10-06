package com.retailpos.payment.repository;

import com.retailpos.payment.entity.PaymentStatus;
import com.retailpos.payment.entity.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Page<PaymentTransaction> findAllBySaleId(Long saleId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p WHERE p.sale.id = :saleId AND p.status = :status")
    BigDecimal sumAmountBySaleIdAndStatus(@Param("saleId") Long saleId, @Param("status") PaymentStatus status);
}
