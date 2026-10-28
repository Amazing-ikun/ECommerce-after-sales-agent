package com.pu.ecommerceaftersalesagent.model;

public enum OrderStatus {

    PENDING_SHIPMENT("待发货"),
    SHIPPED("运输中"),
    DELIVERED("已签收"),
    REFUNDING("退款中"),
    CANCELLED("已取消");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static String labelOf(String code) {
        if (code != null) {
            for (OrderStatus status : values()) {
                if (status.name().equals(code)) {
                    return status.label;
                }
            }
        }
        return code == null ? "未知" : code;
    }
}
