package com.cafepilot.domain.menu.entity;

import com.cafepilot.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "menus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseEntity {

    public enum Category {
        BEVERAGE, FOOD, OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long cafeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(nullable = false)
    private boolean isAvailable;

    @Column(nullable = false)
    private int displayOrder;

    @Column
    private LocalDateTime deletedAt;

    @Version
    private Long version;

    @Builder
    private Menu(Long cafeId, String name, String description,
                 BigDecimal price, Category category, int displayOrder) {
        this.cafeId = cafeId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.isAvailable = true;
        this.displayOrder = displayOrder;
    }

    public static Menu create(Long cafeId, String name, String description,
                              BigDecimal price, Category category, int displayOrder) {
        return Menu.builder()
                .cafeId(cafeId)
                .name(name)
                .description(description)
                .price(price)
                .category(category)
                .displayOrder(displayOrder)
                .build();
    }

    public void update(String name, String description, BigDecimal price,
                       Category category, int displayOrder) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.displayOrder = displayOrder;
    }

    public void markAvailable() {
        this.isAvailable = true;
    }

    public void markUnavailable() {
        this.isAvailable = false;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
