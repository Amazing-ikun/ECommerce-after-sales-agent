package com.pu.ecommerceaftersalesagent.model;

import java.util.List;

public record Shipment(
        String orderNo,
        String carrier,
        String trackingNo,
        String status,
        List<TrackingEvent> events
) {
}
