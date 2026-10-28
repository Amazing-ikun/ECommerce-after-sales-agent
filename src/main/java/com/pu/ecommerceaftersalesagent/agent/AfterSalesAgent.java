package com.pu.ecommerceaftersalesagent.agent;

import com.pu.ecommerceaftersalesagent.guard.FallbackHandler;
import com.pu.ecommerceaftersalesagent.guard.HandoffGuard;
import com.pu.ecommerceaftersalesagent.model.ChatTurn;
import com.pu.ecommerceaftersalesagent.service.ChatSession;
import com.pu.ecommerceaftersalesagent.service.SessionService;
import com.pu.ecommerceaftersalesagent.tool.ToolContextKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AfterSalesAgent {

    private static final Logger log = LoggerFactory.getLogger(AfterSalesAgent.class);

    private final ChatClient chatClient;
    private final SessionService sessionService;
    private final HandoffGuard handoffGuard;
    private final FallbackHandler fallbackHandler;

    public AfterSalesAgent(ChatClient afterSalesChatClient,
                           SessionService sessionService,
                           HandoffGuard handoffGuard,
                           FallbackHandler fallbackHandler) {
        this.chatClient = afterSalesChatClient;
        this.sessionService = sessionService;
        this.handoffGuard = handoffGuard;
        this.fallbackHandler = fallbackHandler;
    }

    public AgentResult chat(String sessionId, String userMessage, String userId) {
        ChatSession session = sessionService.getOrCreate(sessionId, userId);

        if (session.isHumanMode()) {
            String reply = "【人工客服】您的留言已记录，工单号 " + session.ticketId()
                    + "。人工客服会在服务时间内尽快回复您。";
            session.addTurn(userMessage, reply);
            return new AgentResult(reply, true, session.ticketId());
        }

        if (handoffGuard.requestsHuman(userMessage)) {
            String ticket = sessionService.transferToHuman(session, "用户主动请求转人工");
            String reply = handoffReply(ticket);
            session.addTurn(userMessage, reply);
            return new AgentResult(reply, true, ticket);
        }

        try {
            List<Message> messages = buildMessages(session, userMessage);
            String reply = chatClient.prompt()
                    .messages(messages)
                    .toolContext(Map.of(
                            ToolContextKeys.SESSION_ID, sessionId,
                            ToolContextKeys.USER_ID, session.userId()))
                    .call()
                    .content();
            if (reply == null || reply.isBlank()) {
                reply = fallbackHandler.fallbackReply();
            }
            session.addTurn(userMessage, reply);
            return new AgentResult(reply, session.isHumanMode(), session.ticketId());
        } catch (Exception exception) {
            log.error("智能客服调用失败，sessionId={}", sessionId, exception);
            String reply = fallbackHandler.fallbackReply();
            session.addTurn(userMessage, reply);
            return new AgentResult(reply, session.isHumanMode(), session.ticketId());
        }
    }

    private List<Message> buildMessages(ChatSession session, String userMessage) {
        List<Message> messages = new ArrayList<>();
        for (ChatTurn turn : session.recentTurns()) {
            messages.add(new UserMessage(turn.userMessage()));
            messages.add(new AssistantMessage(turn.assistantMessage()));
        }
        messages.add(new UserMessage(userMessage));
        return messages;
    }

    private String handoffReply(String ticket) {
        return "已为您转接人工客服，工单号 " + ticket
                + "。人工客服服务时间为每日 09:00-21:00，会尽快跟进处理；您也可以继续在此留言。";
    }
}
