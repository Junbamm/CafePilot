package com.cafepilot.domain.cafe.repository;

import com.cafepilot.domain.cafe.entity.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe, Long> {

    Optional<Cafe> findByIdAndDeletedAtIsNull(Long id);

    Optional<Cafe> findByOwnerIdAndDeletedAtIsNull(Long ownerId);

    boolean existsByOwnerIdAndDeletedAtIsNull(Long ownerId);
}
