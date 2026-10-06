package com.retailpos.terminal.repository;

import com.retailpos.terminal.entity.Terminal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TerminalRepository extends JpaRepository<Terminal, Long> {

    Optional<Terminal> findByStoreIdAndCode(Long storeId, String code);

    boolean existsByStoreIdAndCode(Long storeId, String code);

    Page<Terminal> findAllByStoreId(Long storeId, Pageable pageable);
}
