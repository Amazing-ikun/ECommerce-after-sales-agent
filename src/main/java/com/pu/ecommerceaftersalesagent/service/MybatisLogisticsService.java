package com.pu.ecommerceaftersalesagent.service;

import com.pu.ecommerceaftersalesagent.mapper.LogisticsMapper;
import com.pu.ecommerceaftersalesagent.mapper.row.ShipmentRow;
import com.pu.ecommerceaftersalesagent.model.Shipment;
import com.pu.ecommerceaftersalesagent.model.ShipmentStatus;
import com.pu.ecommerceaftersalesagent.model.TrackingEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@Profile("mysql")
public class MybatisLogisticsService implements LogisticsService {

    private final LogisticsMapper logisticsMapper;

    public MybatisLogisticsService(LogisticsMapper logisticsMapper) {
        this.logisticsMapper = logisticsMapper;
    }

    @Override
    public List<Shipment> findByOrderNo(String orderNo, String userId) {
        return logisticsMapper.findShipments(normalize(orderNo), userId).stream()
                .map(this::toShipment)
                .toList();
    }

    private Shipment toShipment(ShipmentRow row) {
        List<TrackingEvent> events = logisticsMapper.findRecentEvents(row.getId()).stream()
                .map(event -> new TrackingEvent(
                        event.getEventTime(),
                        event.getLocation(),
                        event.getDescription()))
                .toList();
        return new Shipment(
                row.getOrderNo(),
                row.getCarrierName(),
                row.getTrackingNo(),
                ShipmentStatus.labelOf(row.getStatus()),
                events);
    }

    private static String normalize(String orderNo) {
        return orderNo == null ? "" : orderNo.trim().toUpperCase(Locale.ROOT);
    }
}
