package com.pu.ecommerceaftersalesagent.tool;

import com.pu.ecommerceaftersalesagent.service.ChatSession;
import com.pu.ecommerceaftersalesagent.service.SessionService;
import org.springframework.ai.chat.model.ToolContext;

public final class ToolContextKeys {

    public static final String SESSION_ID = "sessionId";
    public static final String USER_ID = "userId";

    private ToolContextKeys() {
    }

    public static String userId(ToolContext context) {
        return context == null ? null : asString(context.getContext().get(USER_ID));
    }

    public static ChatSession session(ToolContext context, SessionService sessionService) {
        String sessionId = context == null ? null : asString(context.getContext().get(SESSION_ID));
        return sessionId == null ? null : sessionService.find(sessionId).orElse(null);
    }

    private static String asString(Object value) {
        return value instanceof String text && !text.isBlank() ? text : null;
    }
}
