package com.pu.ecommerceaftersalesagent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SessionService {

    private static final DateTimeFormatter TICKET_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
    private final AtomicInteger ticketSequence = new AtomicInteger(1000);
    private final int maxTurns;

    public SessionService(@Value("${app.history-max-turns:10}") int maxTurns) {
        this.maxTurns = maxTurns;
    }

    public ChatSession getOrCreate(String sessionId, String userId) {
        ChatSession session = sessions.computeIfAbsent(sessionId,
                id -> new ChatSession(id, userId, maxTurns));
        if (!session.userId().equals(userId)) {
            throw new IllegalArgumentException("当前会话已绑定其他演示用户，请新建会话");
        }
        return session;
    }

    public Optional<ChatSession> find(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public String transferToHuman(ChatSession session, String reason) {
        synchronized (session) {
            if (session.isHumanMode()) {
                return session.ticketId();
            }
            String ticket = "T" + LocalDate.now().format(TICKET_DATE) + ticketSequence.incrementAndGet();
            session.markHuman(ticket, reason);
            return ticket;
        }
    }

    public void endHumanHandoff(ChatSession session) {
        session.endHumanHandoff();
    }
}
