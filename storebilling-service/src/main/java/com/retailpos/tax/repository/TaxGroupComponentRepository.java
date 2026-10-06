package com.retailpos.tax.repository;

import com.retailpos.tax.entity.TaxGroupComponent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaxGroupComponentRepository extends JpaRepository<TaxGroupComponent, Long> {

    @EntityGraph(attributePaths = {"taxComponent"})
    List<TaxGroupComponent> findAllByTaxGroupId(Long taxGroupId);
}
