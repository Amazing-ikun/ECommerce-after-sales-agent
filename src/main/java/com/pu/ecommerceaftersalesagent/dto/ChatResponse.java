package com.pu.ecommerceaftersalesagent.dto;

public record ChatResponse(String sessionId, String userId, String reply, boolean humanMode, String ticketId) {
}
