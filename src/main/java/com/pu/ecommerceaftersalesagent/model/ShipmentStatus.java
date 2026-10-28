package com.pu.ecommerceaftersalesagent.model;

public enum ShipmentStatus {

    PENDING_PICKUP("待揽收"),
    IN_TRANSIT("运输中"),
    OUT_FOR_DELIVERY("派送中"),
    DELIVERED("已签收"),
    EXCEPTION("物流异常"),
    RETURNING("退货运输中"),
    RETURNED("已退回");

    private final String label;

    ShipmentStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static String labelOf(String code) {
        if (code != null) {
            for (ShipmentStatus status : values()) {
                if (status.name().equals(code)) {
                    return status.label;
                }
            }
        }
        return code == null ? "未知" : code;
    }
}
