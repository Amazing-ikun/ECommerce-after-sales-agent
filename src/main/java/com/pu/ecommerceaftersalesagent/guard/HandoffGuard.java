package com.pu.ecommerceaftersalesagent.guard;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class HandoffGuard {

    private static final Pattern EXPLICIT_REQUEST = Pattern.compile(
            "(转|找|要|换|接)(到|个|一下)?\\s*(人工|真人)(客服)?|^(人工|真人)(客服)?[！!。.？?]?$");

    public boolean requestsHuman(String message) {
        return message != null && EXPLICIT_REQUEST.matcher(message.trim()).find();
    }
}
