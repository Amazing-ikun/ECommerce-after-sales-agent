package com.pu.ecommerceaftersalesagent.service;

import com.pu.ecommerceaftersalesagent.mapper.OrderMapper;
import com.pu.ecommerceaftersalesagent.mapper.row.OrderRow;
import com.pu.ecommerceaftersalesagent.model.Order;
import com.pu.ecommerceaftersalesagent.model.OrderStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Profile("mysql")
public class MybatisOrderService implements OrderService {

    private final OrderMapper orderMapper;

    public MybatisOrderService(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Override
    public List<Order> listByUser(String userId) {
        return orderMapper.listByUserNo(userId).stream()
                .map(this::toOrder)
                .toList();
    }

    @Override
    public Optional<Order> find(String orderNo, String userId) {
        return Optional.ofNullable(orderMapper.findByOrderNoAndUserNo(normalize(orderNo), userId))
                .map(this::toOrder);
    }

    private Order toOrder(OrderRow row) {
        return new Order(
                row.getOrderNo(),
                row.getUserNo(),
                row.getProductName(),
                row.getQuantity() == null ? 0 : row.getQuantity(),
                row.getTotalAmount(),
                OrderStatus.labelOf(row.getStatus()),
                row.getCreatedAt());
    }

    private static String normalize(String orderNo) {
        return orderNo == null ? "" : orderNo.trim().toUpperCase(Locale.ROOT);
    }
}
