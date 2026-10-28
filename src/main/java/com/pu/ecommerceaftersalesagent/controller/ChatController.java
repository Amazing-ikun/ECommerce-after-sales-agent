package com.pu.ecommerceaftersalesagent.controller;

import com.pu.ecommerceaftersalesagent.agent.AfterSalesAgent;
import com.pu.ecommerceaftersalesagent.agent.AgentResult;
import com.pu.ecommerceaftersalesagent.dto.ChatRequest;
import com.pu.ecommerceaftersalesagent.dto.ChatResponse;
import com.pu.ecommerceaftersalesagent.dto.MetaResponse;
import com.pu.ecommerceaftersalesagent.dto.SessionRequest;
import com.pu.ecommerceaftersalesagent.dto.SessionResponse;
import com.pu.ecommerceaftersalesagent.service.ChatSession;
import com.pu.ecommerceaftersalesagent.service.DemoUserService;
import com.pu.ecommerceaftersalesagent.service.SessionService;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ChatController {

    private static final int MAX_MESSAGE_LENGTH = 1000;

    private final AfterSalesAgent afterSalesAgent;
    private final SessionService sessionService;
    private final DemoUserService demoUserService;
    private final Environment environment;

    public ChatController(AfterSalesAgent afterSalesAgent,
                          SessionService sessionService,
                          DemoUserService demoUserService,
                          Environment environment) {
        this.afterSalesAgent = afterSalesAgent;
        this.sessionService = sessionService;
        this.demoUserService = demoUserService;
        this.environment = environment;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody(required = false) ChatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        String message = request.message() == null ? "" : request.message().strip();
        if (message.isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("消息长度不能超过 " + MAX_MESSAGE_LENGTH + " 个字符");
        }
        String sessionId = normalizeSessionId(request.sessionId());
        String userId = demoUserService.resolve(request.userId());
        AgentResult result = afterSalesAgent.chat(sessionId, message, userId);
        return new ChatResponse(sessionId, userId, result.reply(), result.humanMode(), result.ticketId());
    }

    @PostMapping("/handoff/end")
    public SessionResponse endHandoff(@RequestBody(required = false) SessionRequest request) {
        String sessionId = normalizeSessionId(request == null ? null : request.sessionId());
        ChatSession session = sessionService.find(sessionId).orElse(null);
        if (session == null) {
            return new SessionResponse(sessionId, null, false, null, "会话不存在或已失效");
        }
        if (!session.isHumanMode()) {
            return new SessionResponse(sessionId, session.userId(), false, null, "当前不在人工客服模式。");
        }
        sessionService.endHumanHandoff(session);
        String message = "已结束人工客服服务，小星继续为您服务。";
        session.addTurn("结束人工客服", message);
        return new SessionResponse(sessionId, session.userId(), false, null, message);
    }

    @GetMapping("/session")
    public SessionResponse session(@RequestParam String sessionId) {
        return sessionService.find(sessionId)
                .map(session -> new SessionResponse(session.id(), session.userId(),
                        session.isHumanMode(), session.ticketId(), ""))
                .orElseGet(() -> new SessionResponse(sessionId, null, false, null, "会话尚未开始"));
    }

    @GetMapping("/meta")
    public MetaResponse meta() {
        String provider = environment.getProperty("spring.ai.model.chat", "unknown");
        String providerLabel = switch (provider) {
            case "ollama" -> "Ollama 本地";
            case "dashscope" -> "DashScope 通义千问";
            default -> provider;
        };
        String model = switch (provider) {
            case "ollama" -> environment.getProperty("spring.ai.ollama.chat.options.model", "qwen3");
            case "dashscope" -> environment.getProperty("spring.ai.dashscope.chat.options.model", "qwen-plus");
            default -> "unknown";
        };
        boolean mysqlProfile = Arrays.asList(environment.getActiveProfiles()).contains("mysql");
        return new MetaResponse(providerLabel, model,
                environment.getProperty("app.demo-user-id", "U1001"),
                mysqlProfile ? "mysql" : "memory");
    }

    private String normalizeSessionId(String sessionId) {
        return sessionId == null || sessionId.isBlank()
                ? UUID.randomUUID().toString()
                : sessionId.trim();
    }
}
