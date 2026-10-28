package com.pu.ecommerceaftersalesagent.service;

import com.pu.ecommerceaftersalesagent.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    List<Order> listByUser(String userId);

    Optional<Order> find(String orderNo, String userId);
}
