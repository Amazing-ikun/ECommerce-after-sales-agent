package com.pu.ecommerceaftersalesagent.tool;

import com.pu.ecommerceaftersalesagent.model.Order;
import com.pu.ecommerceaftersalesagent.service.OrderService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.List;

@Component
public class OrderQueryTool {

    private final OrderService orderService;

    public OrderQueryTool(OrderService orderService) {
        this.orderService = orderService;
    }

    @Tool(name = "listMyOrders",
            description = "查询当前登录用户的全部订单，返回订单号、商品、数量、实付金额、状态和下单日期。当用户询问“我的订单”但没有提供订单号时调用。")
    public String listMyOrders(ToolContext context) {
        String userId = ToolContextKeys.userId(context);
        if (userId == null) {
            return "系统无法识别当前用户，请重新登录后再试。";
        }
        List<Order> orders = orderService.listByUser(userId);
        if (orders.isEmpty()) {
            return "当前账户下暂无订单。";
        }
        StringBuilder result = new StringBuilder("当前账户共有 ").append(orders.size()).append(" 笔订单：\n");
        for (Order order : orders) {
            result.append("- 订单号 ").append(order.orderNo())
                    .append(" | ").append(order.productName())
                    .append(" ×").append(order.quantity())
                    .append(" | 实付 ¥").append(order.amount().setScale(2, RoundingMode.HALF_UP))
                    .append(" | ").append(order.status())
                    .append(" | 下单日期 ").append(order.createdAt())
                    .append("\n");
        }
        return result.toString();
    }

    @Tool(name = "queryOrder",
            description = "根据订单号查询当前登录用户的订单详情，包括商品、数量、实付金额、订单状态和下单日期。")
    public String queryOrder(
            @ToolParam(description = "订单号，例如 ORD202610001") String orderNo,
            ToolContext context) {
        String userId = ToolContextKeys.userId(context);
        if (userId == null) {
            return "系统无法识别当前用户，请重新登录后再试。";
        }
        if (orderNo == null || orderNo.isBlank()) {
            return "请提供要查询的订单号。";
        }
        return orderService.find(orderNo, userId)
                .map(this::formatOrder)
                .orElse("未找到订单号 " + orderNo.trim() + " 对应的订单，请确认订单号是否正确。");
    }

    private String formatOrder(Order order) {
        return """
                订单号：%s
                商品：%s
                数量：%d
                实付金额：¥%s
                订单状态：%s
                下单日期：%s
                """.formatted(
                order.orderNo(),
                order.productName(),
                order.quantity(),
                order.amount().setScale(2, RoundingMode.HALF_UP),
                order.status(),
                order.createdAt()).strip();
    }
}
