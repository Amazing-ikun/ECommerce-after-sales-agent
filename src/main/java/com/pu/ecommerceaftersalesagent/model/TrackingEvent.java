package com.pu.ecommerceaftersalesagent.model;

import java.time.LocalDateTime;

public record TrackingEvent(LocalDateTime time, String location, String description) {
}
