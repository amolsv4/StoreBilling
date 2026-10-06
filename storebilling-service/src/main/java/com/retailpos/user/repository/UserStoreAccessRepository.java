package com.retailpos.user.repository;

import com.retailpos.user.entity.UserStoreAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserStoreAccessRepository extends JpaRepository<UserStoreAccess, Long> {

    List<UserStoreAccess> findAllByUserId(Long userId);

    boolean existsByUserIdAndStoreId(Long userId, Long storeId);
}
