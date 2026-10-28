package com.pu.ecommerceaftersalesagent.tool;

import com.pu.ecommerceaftersalesagent.model.Order;
import com.pu.ecommerceaftersalesagent.model.Shipment;
import com.pu.ecommerceaftersalesagent.model.TrackingEvent;
import com.pu.ecommerceaftersalesagent.service.LogisticsService;
import com.pu.ecommerceaftersalesagent.service.OrderService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class LogisticsTool {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final OrderService orderService;
    private final LogisticsService logisticsService;

    public LogisticsTool(OrderService orderService, LogisticsService logisticsService) {
        this.orderService = orderService;
        this.logisticsService = logisticsService;
    }

    @Tool(name = "queryLogistics",
            description = "根据订单号查询物流承运商、运单号、物流状态和最新物流轨迹。用户询问“快递到哪了”“什么时候到”“物流怎么不更新”时调用。")
    public String queryLogistics(
            @ToolParam(description = "订单号，例如 ORD202610002") String orderNo,
            ToolContext context) {
        String userId = ToolContextKeys.userId(context);
        if (userId == null) {
            return "系统无法识别当前用户，请重新登录后再试。";
        }
        if (orderNo == null || orderNo.isBlank()) {
            return "请提供要查询物流的订单号。";
        }
        Optional<Order> order = orderService.find(orderNo, userId);
        if (order.isEmpty()) {
            return "未找到订单号 " + orderNo.trim() + " 对应的订单，请确认订单号是否正确。";
        }
        List<Shipment> shipments = logisticsService.findByOrderNo(orderNo, userId);
        if (shipments.isEmpty()) {
            return "订单 " + order.get().orderNo() + " 当前状态为「" + order.get().status()
                    + "」，物流单号尚未生成，发货后可再次查询。";
        }
        return formatShipments(shipments);
    }

    private String formatShipments(List<Shipment> shipments) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < shipments.size(); index++) {
            Shipment shipment = shipments.get(index);
            if (shipments.size() > 1) {
                result.append("包裹 ").append(index + 1).append("/").append(shipments.size()).append("：\n");
            }
            result.append("订单号：").append(shipment.orderNo()).append("\n")
                    .append("承运商：").append(shipment.carrier()).append("\n")
                    .append("运单号：").append(shipment.trackingNo()).append("\n")
                    .append("物流状态：").append(shipment.status()).append("\n")
                    .append("最新轨迹：\n");
            for (TrackingEvent event : shipment.events()) {
                result.append("- ")
                        .append(event.time().format(TIME_FORMAT))
                        .append("  ")
                        .append(event.location() == null ? "" : event.location())
                        .append("  ")
                        .append(event.description())
                        .append("\n");
            }
            if (index < shipments.size() - 1) {
                result.append("\n");
            }
        }
        return result.toString().strip();
    }
}
