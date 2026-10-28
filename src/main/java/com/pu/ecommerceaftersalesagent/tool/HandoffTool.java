package com.pu.ecommerceaftersalesagent.tool;

import com.pu.ecommerceaftersalesagent.service.ChatSession;
import com.pu.ecommerceaftersalesagent.service.SessionService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class HandoffTool {

    private final SessionService sessionService;

    public HandoffTool(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Tool(name = "transferToHuman",
            description = "将当前会话转接给人工客服。当用户明确要求转人工，或问题涉及投诉、赔偿、账户资金安全、智能客服多轮仍无法解决时调用。",
            returnDirect = true)
    public String transferToHuman(
            @ToolParam(description = "转人工原因，简要描述用户的问题") String reason,
            ToolContext context) {
        ChatSession session = ToolContextKeys.session(context, sessionService);
        if (session == null) {
            return "系统无法识别当前会话，请刷新页面后重试。";
        }
        String ticket = sessionService.transferToHuman(session,
                reason == null || reason.isBlank() ? "用户请求人工客服" : reason);
        return "已为您转接人工客服，工单号 " + ticket
                + "。人工客服服务时间为每日 09:00-21:00，会尽快跟进处理；您也可以继续在此留言。";
    }
}
