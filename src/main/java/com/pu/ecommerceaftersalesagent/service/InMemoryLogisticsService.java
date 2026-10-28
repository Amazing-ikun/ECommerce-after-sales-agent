package com.pu.ecommerceaftersalesagent.service;

import com.pu.ecommerceaftersalesagent.model.Shipment;
import com.pu.ecommerceaftersalesagent.model.TrackingEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("!mysql")
public class InMemoryLogisticsService implements LogisticsService {

    private final Map<String, Shipment> shipments = new ConcurrentHashMap<>();

    public InMemoryLogisticsService() {
        seed();
    }

    @Override
    public List<Shipment> findByOrderNo(String orderNo, String userId) {
        Shipment shipment = shipments.get(normalize(orderNo));
        return shipment == null ? List.of() : List.of(shipment);
    }

    private void seed() {
        shipments.put("ORD202610001", new Shipment("ORD202610001", "顺丰速运", "SF1438792650012", "已签收", List.of(
                new TrackingEvent(LocalDateTime.of(2026, 10, 8, 10, 12), "上海市浦东新区", "已签收，签收人：本人"),
                new TrackingEvent(LocalDateTime.of(2026, 10, 8, 8, 2), "上海市浦东新区", "快件正在派送中"),
                new TrackingEvent(LocalDateTime.of(2026, 10, 7, 21, 15), "上海转运中心", "快件已到达上海转运中心"),
                new TrackingEvent(LocalDateTime.of(2026, 10, 7, 14, 30), "杭州收件点", "顺丰速运已收取快件")
        )));

        shipments.put("ORD202610002", new Shipment("ORD202610002", "中通快递", "ZTO785412369", "运输中", List.of(
                new TrackingEvent(LocalDateTime.of(2026, 10, 23, 9, 40), "上海转运中心", "快件已到达上海转运中心，准备派送"),
                new TrackingEvent(LocalDateTime.of(2026, 10, 22, 18, 20), "杭州分拨中心", "快件已由杭州分拨中心发出"),
                new TrackingEvent(LocalDateTime.of(2026, 10, 22, 11, 5), "杭州西湖区", "中通快递已揽收")
        )));

        shipments.put("ORD202610004", new Shipment("ORD202610004", "京东物流", "JDVA00627815421", "退货运输中", List.of(
                new TrackingEvent(LocalDateTime.of(2026, 10, 22, 16, 25), "上海退货仓", "退货商品已签收，等待质检"),
                new TrackingEvent(LocalDateTime.of(2026, 10, 21, 10, 8), "上海市浦东新区", "快递员已取走退货包裹"),
                new TrackingEvent(LocalDateTime.of(2026, 10, 20, 19, 42), "上海市浦东新区", "已预约上门取件")
        )));

        shipments.put("ORD202610005", new Shipment("ORD202610005", "顺丰速运", "SF9988776655443", "已签收", List.of(
                new TrackingEvent(LocalDateTime.of(2026, 10, 17, 14, 20), "杭州市西湖区", "已签收，签收人：李娜"),
                new TrackingEvent(LocalDateTime.of(2026, 10, 17, 9, 10), "杭州市西湖区", "快件正在派送中"),
                new TrackingEvent(LocalDateTime.of(2026, 10, 16, 20, 15), "杭州转运中心", "快件已到达杭州转运中心"),
                new TrackingEvent(LocalDateTime.of(2026, 10, 16, 10, 0), "杭州收件点", "顺丰速运已收取快件")
        )));

        shipments.put("ORD202610006", new Shipment("ORD202610006", "中通快递", "ZTO998877665", "运输中", List.of(
                new TrackingEvent(LocalDateTime.of(2026, 10, 25, 8, 30), "上海转运中心", "快件已到达上海转运中心"),
                new TrackingEvent(LocalDateTime.of(2026, 10, 24, 18, 0), "杭州分拨中心", "快件已由杭州分拨中心发出"),
                new TrackingEvent(LocalDateTime.of(2026, 10, 24, 10, 0), "杭州西湖区", "中通快递已揽收")
        )));
    }

    private static String normalize(String orderNo) {
        return orderNo == null ? "" : orderNo.trim().toUpperCase(Locale.ROOT);
    }
}
