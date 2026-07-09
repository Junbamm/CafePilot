package com.cafepilot.domain.order.entity;

import com.cafepilot.domain.order.exception.OrderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Order 엔티티 단위 테스트")
class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.create(1L, 1L, "추가 요청 없음");
        order.addItem(1L, "아메리카노", new BigDecimal("4500"), 2);
        order.addItem(2L, "카페라떼", new BigDecimal("5000"), 1);
    }

    @Test
    @DisplayName("주문 생성 시 PENDING 상태이고 금액이 정확히 계산된다")
    void createOrder() {
        assertThat(order.getStatus()).isEqualTo(Order.Status.PENDING);
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("14000"));
        assertThat(order.getItems()).hasSize(2);
    }

    @Test
    @DisplayName("PENDING → ACCEPTED 상태 전이가 성공한다")
    void accept() {
        order.accept();
        assertThat(order.getStatus()).isEqualTo(Order.Status.ACCEPTED);
    }

    @Test
    @DisplayName("ACCEPTED → PREPARING 상태 전이가 성공한다")
    void startPreparing() {
        order.accept();
        order.startPreparing();
        assertThat(order.getStatus()).isEqualTo(Order.Status.PREPARING);
    }

    @Test
    @DisplayName("PREPARING → COMPLETED 상태 전이가 성공한다")
    void complete() {
        order.accept();
        order.startPreparing();
        order.complete();
        assertThat(order.getStatus()).isEqualTo(Order.Status.COMPLETED);
    }

    @Test
    @DisplayName("PENDING 상태에서 바로 PREPARING으로 전이하면 예외가 발생한다")
    void invalidStatusTransition() {
        assertThatThrownBy(order::startPreparing)
                .isInstanceOf(OrderException.class);
    }

    @Test
    @DisplayName("COMPLETED 상태에서 취소하면 예외가 발생한다")
    void cannotCancelCompleted() {
        order.accept();
        order.startPreparing();
        order.complete();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderException.class);
    }

    @Test
    @DisplayName("PENDING 상태에서 취소할 수 있다")
    void cancelFromPending() {
        order.cancel();
        assertThat(order.getStatus()).isEqualTo(Order.Status.CANCELLED);
    }
}
