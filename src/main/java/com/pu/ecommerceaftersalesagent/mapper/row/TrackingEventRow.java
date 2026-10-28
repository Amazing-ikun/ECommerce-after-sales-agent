package com.pu.ecommerceaftersalesagent.mapper.row;

import java.time.LocalDateTime;

public class TrackingEventRow {

    private LocalDateTime eventTime;
    private String location;
    private String description;

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
