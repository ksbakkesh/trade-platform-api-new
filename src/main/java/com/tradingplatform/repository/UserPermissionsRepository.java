package com.tradingplatform.repository;

import com.tradingplatform.domain.UserPermissions;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserPermissionsRepository extends JpaRepository<UserPermissions, Long> {
    Optional<UserPermissions> findByUserId(Long userId);
}