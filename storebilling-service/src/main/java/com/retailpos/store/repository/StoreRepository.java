package com.retailpos.store.repository;

import com.retailpos.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByCode(String code);

    boolean existsByCode(String code);

    Page<Store> findAllByOrganizationId(Long organizationId, Pageable pageable);
}
