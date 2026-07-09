package com.cafepilot.domain.order.entity;

import com.cafepilot.domain.order.exception.OrderException;
import com.cafepilot.global.entity.BaseEntity;
import com.cafepilot.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    public enum Status {
        PENDING, ACCEPTED, PREPARING, COMPLETED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long cafeId;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 500)
    private String note;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Version
    private Long version;

    private Order(Long cafeId, Long memberId, String note) {
        this.cafeId = cafeId;
        this.memberId = memberId;
        this.status = Status.PENDING;
        this.totalAmount = BigDecimal.ZERO;
        this.note = note;
    }

    public static Order create(Long cafeId, Long memberId, String note) {
        return new Order(cafeId, memberId, note);
    }

    public void addItem(Long menuId, String menuName, BigDecimal unitPrice, int quantity) {
        OrderItem item = OrderItem.of(this, menuId, menuName, unitPrice, quantity);
        this.items.add(item);
        this.totalAmount = this.totalAmount.add(item.getSubtotal());
    }

    public void accept() {
        validateStatus(Status.PENDING);
        this.status = Status.ACCEPTED;
    }

    public void startPreparing() {
        validateStatus(Status.ACCEPTED);
        this.status = Status.PREPARING;
    }

    public void complete() {
        validateStatus(Status.PREPARING);
        this.status = Status.COMPLETED;
    }

    public void cancel() {
        if (this.status == Status.COMPLETED) {
            throw new OrderException(ErrorCode.ORDER_INVALID_STATUS);
        }
        this.status = Status.CANCELLED;
    }

    private void validateStatus(Status required) {
        if (this.status != required) {
            throw new OrderException(ErrorCode.ORDER_INVALID_STATUS);
        }
    }
}
