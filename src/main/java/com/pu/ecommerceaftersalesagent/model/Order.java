package com.pu.ecommerceaftersalesagent.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Order(
        String orderNo,
        String userId,
        String productName,
        int quantity,
        BigDecimal amount,
        String status,
        LocalDate createdAt
) {
}
