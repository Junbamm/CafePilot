package com.cafepilot.domain.menu.repository;

import com.cafepilot.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByCafeIdAndDeletedAtIsNullOrderByDisplayOrderAsc(Long cafeId);

    Optional<Menu> findByIdAndDeletedAtIsNull(Long id);
}
