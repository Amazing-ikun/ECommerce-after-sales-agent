package com.pu.ecommerceaftersalesagent.service;

import com.pu.ecommerceaftersalesagent.model.Shipment;

import java.util.List;

public interface LogisticsService {

    List<Shipment> findByOrderNo(String orderNo, String userId);
}
