package com.pu.ecommerceaftersalesagent.service;

import com.pu.ecommerceaftersalesagent.model.ChatTurn;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class ChatSession {

    private final String id;
    private final String userId;
    private final int maxTurns;
    private final Deque<ChatTurn> turns = new ArrayDeque<>();
    private final Instant createdAt = Instant.now();
    private boolean humanMode;
    private String ticketId;
    private String handoffReason;
    private Instant lastActiveAt = createdAt;

    ChatSession(String id, String userId, int maxTurns) {
        this.id = id;
        this.userId = userId;
        this.maxTurns = maxTurns;
    }

    public String id() {
        return id;
    }

    public String userId() {
        return userId;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public synchronized Instant lastActiveAt() {
        return lastActiveAt;
    }

    public synchronized List<ChatTurn> recentTurns() {
        return List.copyOf(turns);
    }

    public synchronized void addTurn(String userMessage, String assistantMessage) {
        turns.addLast(new ChatTurn(userMessage, assistantMessage));
        while (turns.size() > maxTurns) {
            turns.removeFirst();
        }
        lastActiveAt = Instant.now();
    }

    public synchronized boolean isHumanMode() {
        return humanMode;
    }

    public synchronized String ticketId() {
        return ticketId;
    }

    public synchronized String handoffReason() {
        return handoffReason;
    }

    public synchronized void markHuman(String ticketId, String reason) {
        if (humanMode) {
            return;
        }
        this.humanMode = true;
        this.ticketId = ticketId;
        this.handoffReason = reason;
    }

    public synchronized void endHumanHandoff() {
        this.humanMode = false;
        this.ticketId = null;
        this.handoffReason = null;
    }
}
