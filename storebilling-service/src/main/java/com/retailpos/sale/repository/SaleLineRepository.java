package com.retailpos.sale.repository;

import com.retailpos.sale.entity.SaleLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleLineRepository extends JpaRepository<SaleLine, Long> {

    List<SaleLine> findAllBySaleId(Long saleId);
}
