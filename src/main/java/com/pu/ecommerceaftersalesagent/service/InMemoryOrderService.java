package com.pu.ecommerceaftersalesagent.service;

import com.pu.ecommerceaftersalesagent.model.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@Profile("!mysql")
public class InMemoryOrderService implements OrderService {

    private final Map<String, Order> orders = new LinkedHashMap<>();

    public InMemoryOrderService() {
        seed();
    }

    @Override
    public List<Order> listByUser(String userId) {
        return orders.values().stream()
                .filter(order -> order.userId().equals(userId))
                .toList();
    }

    @Override
    public Optional<Order> find(String orderNo, String userId) {
        return Optional.ofNullable(orders.get(normalize(orderNo)))
                .filter(order -> order.userId().equals(userId));
    }

    private void seed() {
        add(new Order("ORD202610001", "U1001", "星河无线降噪耳机 Pro", 1,
                new BigDecimal("899.00"), "已签收", LocalDate.of(2026, 10, 8)));
        add(new Order("ORD202610002", "U1001", "家用多功能空气炸锅 5L", 1,
                new BigDecimal("399.00"), "运输中", LocalDate.of(2026, 10, 20)));
        add(new Order("ORD202610003", "U1001", "轻量缓震跑步鞋", 2,
                new BigDecimal("658.00"), "待发货", LocalDate.of(2026, 10, 23)));
        add(new Order("ORD202610004", "U1001", "星河智能手表 S2", 1,
                new BigDecimal("1299.00"), "退款中", LocalDate.of(2026, 10, 2)));
        add(new Order("ORD202610005", "U1002", "意式半自动咖啡机", 1,
                new BigDecimal("1599.00"), "已签收", LocalDate.of(2026, 10, 15)));
        add(new Order("ORD202610006", "U1002", "罗技机械键盘 K845", 1,
                new BigDecimal("499.00"), "运输中", LocalDate.of(2026, 10, 23)));
    }

    private void add(Order order) {
        orders.put(order.orderNo(), order);
    }

    private static String normalize(String orderNo) {
        return orderNo == null ? "" : orderNo.trim().toUpperCase(Locale.ROOT);
    }
}
