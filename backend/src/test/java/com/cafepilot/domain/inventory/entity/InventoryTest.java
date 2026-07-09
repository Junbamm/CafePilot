package com.cafepilot.domain.inventory.entity;

import com.cafepilot.domain.inventory.exception.InventoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Inventory 엔티티 단위 테스트")
class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = Inventory.create(1L, 1L, 10, 3);
    }

    @Test
    @DisplayName("재고를 증가시킬 수 있다")
    void increase() {
        inventory.increase(5);
        assertThat(inventory.getQuantity()).isEqualTo(15);
    }

    @Test
    @DisplayName("재고를 차감할 수 있다")
    void decrease() {
        inventory.decrease(3);
        assertThat(inventory.getQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("재고가 부족하면 예외가 발생한다")
    void decreaseInsufficientStock() {
        assertThatThrownBy(() -> inventory.decrease(11))
                .isInstanceOf(InventoryException.class);
    }

    @Test
    @DisplayName("증가량이 0 이하이면 예외가 발생한다")
    void increaseWithZero() {
        assertThatThrownBy(() -> inventory.increase(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("차감 후 임계값 이하이면 isLowStock이 true다")
    void isLowStock() {
        inventory.decrease(8);
        assertThat(inventory.getQuantity()).isEqualTo(2);
        assertThat(inventory.isLowStock()).isTrue();
    }

    @Test
    @DisplayName("재고가 임계값보다 많으면 isLowStock이 false다")
    void isNotLowStock() {
        assertThat(inventory.isLowStock()).isFalse();
    }

    @Test
    @DisplayName("임계값을 변경할 수 있다")
    void updateThreshold() {
        inventory.updateThreshold(5);
        assertThat(inventory.getLowStockThreshold()).isEqualTo(5);
        assertThat(inventory.isLowStock()).isTrue();
    }
}
