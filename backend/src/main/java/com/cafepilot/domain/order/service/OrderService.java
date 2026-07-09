package com.cafepilot.domain.order.service;

import com.cafepilot.domain.menu.entity.Menu;
import com.cafepilot.domain.menu.exception.MenuException;
import com.cafepilot.domain.menu.repository.MenuRepository;
import com.cafepilot.domain.order.dto.CreateOrderRequest;
import com.cafepilot.domain.order.dto.OrderResponse;
import com.cafepilot.domain.order.entity.Order;
import com.cafepilot.domain.order.event.OrderCreatedEvent;
import com.cafepilot.domain.order.exception.OrderException;
import com.cafepilot.domain.order.repository.OrderRepository;
import com.cafepilot.global.config.RabbitMqConfig;
import com.cafepilot.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final RabbitTemplate rabbitTemplate;

    public Page<OrderResponse> getOrders(Long cafeId, Pageable pageable) {
        return orderRepository.findByCafeIdOrderByCreatedAtDesc(cafeId, pageable)
                .map(OrderResponse::from);
    }

    public OrderResponse getOrder(Long cafeId, Long orderId) {
        Order order = orderRepository.findByIdAndCafeId(orderId, cafeId)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse createOrder(Long memberId, Long cafeId, CreateOrderRequest request) {
        Order order = Order.create(cafeId, memberId, request.note());

        for (var itemRequest : request.items()) {
            Menu menu = menuRepository.findByIdAndDeletedAtIsNull(itemRequest.menuId())
                    .orElseThrow(() -> new MenuException(ErrorCode.MENU_NOT_FOUND));

            if (!menu.isAvailable()) {
                throw new MenuException(ErrorCode.MENU_NOT_AVAILABLE);
            }

            order.addItem(menu.getId(), menu.getName(), menu.getPrice(), itemRequest.quantity());
        }

        orderRepository.save(order);

        publishOrderCreatedEvent(order);

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse changeStatus(Long cafeId, Long orderId, String action) {
        Order order = orderRepository.findByIdAndCafeId(orderId, cafeId)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));

        switch (action.toUpperCase()) {
            case "ACCEPT"   -> order.accept();
            case "PREPARE"  -> order.startPreparing();
            case "COMPLETE" -> order.complete();
            case "CANCEL"   -> order.cancel();
            default -> throw new OrderException(ErrorCode.ORDER_INVALID_STATUS);
        }

        return OrderResponse.from(order);
    }

    private void publishOrderCreatedEvent(Order order) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.ORDER_EXCHANGE,
                    RabbitMqConfig.ORDER_CREATED_ROUTING_KEY,
                    new OrderCreatedEvent(order.getId(), order.getCafeId(),
                            order.getMemberId(), order.getTotalAmount())
            );
        } catch (Exception e) {
            log.warn("[OrderService] RabbitMQ 이벤트 발행 실패 orderId={}", order.getId(), e);
        }
    }
}
