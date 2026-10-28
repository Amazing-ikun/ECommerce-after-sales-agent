package com.pu.ecommerceaftersalesagent.dto;

public record SessionResponse(String sessionId, String userId, boolean humanMode, String ticketId, String message) {
}
