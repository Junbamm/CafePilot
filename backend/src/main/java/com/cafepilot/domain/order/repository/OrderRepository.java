package com.cafepilot.domain.order.repository;

import com.cafepilot.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCafeIdOrderByCreatedAtDesc(Long cafeId, Pageable pageable);

    Optional<Order> findByIdAndCafeId(Long id, Long cafeId);
}
